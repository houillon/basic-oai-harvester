package fr.persee.oai.harvest;

import fr.persee.oai.domain.request.OaiRequest;
import fr.persee.oai.domain.request.OaiTimeBoundary;
import fr.persee.oai.domain.response.OaiGranularity;
import fr.persee.oai.domain.response.OaiHeader;
import fr.persee.oai.domain.response.OaiResponse;
import fr.persee.oai.domain.response.OaiResumptionToken;
import fr.persee.oai.harvest.http.RequestService;
import fr.persee.oai.harvest.status.HarvestStatus;
import fr.persee.oai.harvest.status.HarvestTrack;
import fr.persee.oai.harvest.status.StatusService;
import fr.persee.oai.harvest.status.TrackStatus;
import java.net.URI;
import java.nio.file.Path;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Element;

@RequiredArgsConstructor
@Slf4j
public class Harvester {

  private static final String DC_PREFIX = "oai_dc";

  private final RequestService requestService;
  private final StatusService statusService;

  public void harvest(
      URI baseUrl,
      Set<String> metadataPrefixes,
      List<String> sets,
      @Nullable OaiTimeBoundary from,
      @Nullable OaiTimeBoundary until,
      Path path) {
    OaiRequest.Identify identify = new OaiRequest.Identify(baseUrl);

    try {
      RequestService.Timestamped<OaiResponse.Body.Identify> response =
          requestService.identify(identify);
      OaiGranularity granularity = response.value().granularity();

      from = fixTimeBoundary(from, granularity, "from");
      until = fixTimeBoundary(until, granularity, "until");

      HarvestStatus status =
          HarvestStatus.forNewHarvest(
              response.timestamp(), baseUrl, metadataPrefixes, sets, from, until);

      statusService.write(status, path);

      List<OaiRequest.ListIdentifiers> initialRequests =
          buildInitialRequests(baseUrl, sets, from, until);

      for (OaiRequest.ListIdentifiers initialRequest : initialRequests) {
        new Instance(baseUrl, metadataPrefixes, path, granularity).harvest(initialRequest);
      }
    } catch (RequestService.OaiRequestError e) {
      log.atWarn().log("error executing identify: {} : {}", identify, e.errors());
    }
  }

  private static @Nullable OaiTimeBoundary fixTimeBoundary(
      @Nullable OaiTimeBoundary boundary, OaiGranularity granularity, String name) {
    if (granularity == OaiGranularity.SECOND && boundary instanceof OaiTimeBoundary.Date d) {
      log.atWarn()
          .log(
              "%s is a date but repository granularity is second, using UTC start of day"
                  .formatted(name));
      return new OaiTimeBoundary.DateTime(d.date().atStartOfDay(ZoneOffset.UTC).toInstant());
    }

    return boundary;
  }

  private List<OaiRequest.ListIdentifiers> buildInitialRequests(
      URI baseUrl,
      Collection<String> sets,
      @Nullable OaiTimeBoundary from,
      @Nullable OaiTimeBoundary until) {
    if (sets.isEmpty()) {
      return List.of(OaiRequest.ListIdentifiers.of(baseUrl, DC_PREFIX, null, from, until));
    }

    return sets.stream()
        .map(s -> OaiRequest.ListIdentifiers.of(baseUrl, DC_PREFIX, s, from, until))
        .toList();
  }

  @RequiredArgsConstructor
  private class Instance {

    private final Queue<OaiRequest.ListIdentifiers> queue = new ArrayDeque<>();

    private final URI baseUrl;
    private final Set<String> metadataPrefixes;
    private final Path path;

    private final OaiGranularity granularity;

    private void harvest(OaiRequest.ListIdentifiers initialRequest) {
      queue(initialRequest);

      while (!Thread.interrupted() && !queue.isEmpty()) {
        OaiResponse.Body.ListIdentifiers responseBody = executeListIdentifiers(queue.remove());
        if (responseBody != null) {
          handleListIdentifiers(responseBody);
          updateStatus(initialRequest, responseBody);
        }
      }
    }

    private void updateStatus(
        OaiRequest.ListIdentifiers initialRequest, OaiResponse.Body.ListIdentifiers responseBody) {
      HarvestTrack track = buildTrack(initialRequest.set());
      TrackStatus status = buildStatus(responseBody.resumptionToken());
      statusService.update(track, status, path);
    }

    private TrackStatus buildStatus(@Nullable OaiResumptionToken resumptionToken) {
      if (resumptionToken == null) return TrackStatus.Done.INSTANCE;

      return new TrackStatus.InProgress(resumptionToken.content());
    }

    private HarvestTrack buildTrack(@Nullable String set) {
      if (set == null) return HarvestTrack.Full.INSTANCE;

      return new HarvestTrack.Set(set);
    }

    private void queue(OaiRequest.ListIdentifiers request) {
      log.atDebug().log("queueing request: {}", request);
      queue.add(request);
    }

    private OaiResponse.Body.@Nullable ListIdentifiers executeListIdentifiers(
        OaiRequest.ListIdentifiers request) {
      try {
        log.atInfo().log("executing list identifiers: {}", request);
        OaiResponse.Body.ListIdentifiers listIdentifiers =
            requestService.listIdentifiers(request, granularity);
        log.atInfo().log(
            "list identifiers response elements: {} ; resumption token: {}",
            listIdentifiers.headers().size(),
            listIdentifiers.resumptionToken());
        return listIdentifiers;
      } catch (RequestService.OaiRequestError e) {
        log.atWarn().log("error executing list identifiers: {} : {}", request, e.errors());

        return null;
      }
    }

    private void handleListIdentifiers(OaiResponse.Body.ListIdentifiers body) {
      for (OaiHeader oaiHeader : body.headers()) {
        if (Thread.currentThread().isInterrupted()) return;

        handleIdentifier(oaiHeader);
      }

      OaiResumptionToken resumptionToken = body.resumptionToken();
      if (resumptionToken != null) handleResumptionToken(resumptionToken);
    }

    private void handleResumptionToken(OaiResumptionToken resumptionToken) {
      log.atDebug().log("handling resumption token: {}", resumptionToken.content());

      OaiRequest.ListIdentifiers listIdentifiers =
          OaiRequest.ListIdentifiers.of(baseUrl, resumptionToken.content());

      queue(listIdentifiers);
    }

    private void handleIdentifier(OaiHeader oaiHeader) {
      log.atDebug().log("handling identifier: {}", oaiHeader.identifier());

      for (String metadataPrefix : getPrefixes()) {
        if (Thread.currentThread().isInterrupted()) return;

        OaiRequest.GetRecord getRecord =
            new OaiRequest.GetRecord(baseUrl, oaiHeader.identifier(), metadataPrefix);

        executeGetRecord(getRecord);
      }
    }

    private Set<String> getPrefixes() {
      if (metadataPrefixes.isEmpty()) return Set.of(DC_PREFIX);

      return metadataPrefixes;
    }

    private void executeGetRecord(OaiRequest.GetRecord getRecord) {
      try {
        OaiResponse.Body.GetRecord response = requestService.getRecord(getRecord);

        handleGetRecord(response, getRecord.metadataPrefix());
      } catch (RequestService.OaiRequestError e) {
        log.atWarn().log("error executing get record: {} : {}", getRecord, e.errors());
      }
    }

    private void handleGetRecord(OaiResponse.Body.GetRecord body, String prefix) {
      log.atDebug().log("handling get record: {}", body);

      Element metadata = body.content().metadata();
      if (metadata == null) {
        log.atWarn().log("no metadata for get record: {}", body.content());
        return;
      }

      FileWriter.write(path, metadata, body.content().header().identifier(), prefix);
    }
  }
}
