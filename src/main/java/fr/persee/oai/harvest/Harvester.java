package fr.persee.oai.harvest;

import fr.persee.oai.domain.request.OaiRequest;
import fr.persee.oai.domain.request.OaiTimeBoundary;
import fr.persee.oai.domain.response.OaiGranularity;
import fr.persee.oai.domain.response.OaiHeader;
import fr.persee.oai.domain.response.OaiResponse;
import fr.persee.oai.domain.response.OaiResumptionToken;
import fr.persee.oai.harvest.http.RequestService;
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

  public void harvest(
      URI baseUrl,
      Set<String> metadataPrefixes,
      List<String> sets,
      @Nullable OaiTimeBoundary from,
      @Nullable OaiTimeBoundary until,
      Path path) {
    OaiRequest.Identify identify = new OaiRequest.Identify(baseUrl);

    try {
      OaiResponse.Body.Identify response = requestService.identify(identify);

      from = fixTimeBoundary(from, response.granularity(), "from");
      until = fixTimeBoundary(until, response.granularity(), "until");

      List<OaiRequest.ListIdentifiers> initialRequests =
          buildInitialRequests(baseUrl, sets, from, until);

      for (OaiRequest.ListIdentifiers initialRequest : initialRequests) {
        new Instance(baseUrl, metadataPrefixes, path, response.granularity())
            .harvest(initialRequest);
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
        if (responseBody != null) handleListIdentifiers(responseBody);
      }
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
