package fr.persee.oai.harvest;

import static java.util.Comparator.comparing;

import fr.persee.oai.domain.request.OaiRequest;
import fr.persee.oai.domain.request.OaiTimeBoundary;
import fr.persee.oai.domain.response.OaiErrorCode;
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
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    RequestService.Timestamped<OaiResponse.Body.Identify> identify =
        getTimestampedIdentify(baseUrl);

    OaiGranularity granularity = identify.value().granularity();

    from = fixTimeBoundary(from, granularity, "from");
    until = fixTimeBoundary(until, granularity, "until");

    OaiTimeBoundary started = buildStartBoundary(identify.timestamp(), granularity);

    HarvestStatus status =
        HarvestStatus.forNewHarvest(started, baseUrl, metadataPrefixes, sets, from, until);

    statusService.write(status, path);

    launchHarvest(path, status, granularity);
  }

  private RequestService.Timestamped<OaiResponse.Body.Identify> getTimestampedIdentify(
      URI baseUrl) {
    OaiRequest.Identify identify = new OaiRequest.Identify(baseUrl);
    return requestService.identify(identify);
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

  // suppressing nullaway until it can handle this switch expression
  @SuppressWarnings("NullAway")
  private OaiTimeBoundary buildStartBoundary(Instant timestamp, OaiGranularity granularity) {
    return switch (granularity) {
      case OaiGranularity.DAY -> new OaiTimeBoundary.Date(
          timestamp.atZone(ZoneOffset.UTC).toLocalDate());
      case OaiGranularity.SECOND -> new OaiTimeBoundary.DateTime(timestamp);
    };
  }

  public void resume(Path path) {
    HarvestStatus status = statusService.read(path);
    if (status == null) {
      log.atError().log("no status file found: {}", path);
      return;
    }

    OaiGranularity granularity = getTimestampedIdentify(status.baseUrl()).value().granularity();

    launchHarvest(path, status, granularity);
  }

  private void launchHarvest(Path path, HarvestStatus status, OaiGranularity granularity) {
    status.trackStatuses().entrySet().stream()
        .sorted(Map.Entry.comparingByValue(comparing(this::trackStatusPosition)))
        .forEach(e -> launchTrackHarvest(path, e.getKey(), e.getValue(), status, granularity));
  }

  private int trackStatusPosition(TrackStatus status) {
    return switch (status) {
      case TrackStatus.InProgress __ -> 0;
      case TrackStatus.Pending __ -> 1;
      case TrackStatus.Done __ -> 2;
    };
  }

  private void launchTrackHarvest(
      Path path,
      HarvestTrack track,
      TrackStatus trackStatus,
      HarvestStatus status,
      OaiGranularity granularity) {
    OaiRequest.ListIdentifiers listIdentifiers = buildRequest(track, trackStatus, status);

    if (listIdentifiers == null) return;

    new Instance(status.baseUrl(), status.metadataPrefixes(), path, granularity)
        .harvest(listIdentifiers, track);
  }

  private OaiRequest.@Nullable ListIdentifiers buildRequest(
      HarvestTrack track, TrackStatus trackStatus, HarvestStatus status) {
    return switch (trackStatus) {
      case TrackStatus.InProgress progress -> buildResumeRequest(progress, status);
      case TrackStatus.Pending __ -> buildHarvestRequest(track, status);
      case TrackStatus.Done __ -> null;
    };
  }

  private OaiRequest.ListIdentifiers buildResumeRequest(
      TrackStatus.InProgress progress, HarvestStatus status) {
    return OaiRequest.ListIdentifiers.of(status.baseUrl(), progress.resumptionToken());
  }

  private OaiRequest.ListIdentifiers buildHarvestRequest(HarvestTrack track, HarvestStatus status) {
    String set =
        switch (track) {
          case HarvestTrack.Full __ -> null;
          case HarvestTrack.Set s -> s.name();
        };

    return OaiRequest.ListIdentifiers.of(
        status.baseUrl(), DC_PREFIX, set, status.from(), status.until());
  }

  public void update(Path path) {
    HarvestStatus status = statusService.read(path);
    if (status == null) {
      log.atError().log("no status file found: {}", path);
      return;
    }

    boolean allDone =
        status.trackStatuses().values().stream().allMatch(TrackStatus.Done.class::isInstance);

    if (!allDone) {
      log.atWarn().log("previous harvest is not complete, use resume first");
      return;
    }

    List<String> sets = getSets(status);

    harvest(
        status.baseUrl(), status.metadataPrefixes(), sets, status.started(), status.until(), path);
  }

  private List<String> getSets(HarvestStatus status) {
    if (status.trackStatuses().containsKey(HarvestTrack.Full.INSTANCE)) return List.of();

    return status.trackStatuses().keySet().stream()
        .map(this::mapSetTrack)
        .filter(Objects::nonNull)
        .toList();
  }

  private @Nullable String mapSetTrack(HarvestTrack track) {
    return switch (track) {
      case HarvestTrack.Full __ -> null;
      case HarvestTrack.Set set -> set.name();
    };
  }

  @RequiredArgsConstructor
  private class Instance {

    private final Queue<OaiRequest.ListIdentifiers> queue = new ArrayDeque<>();

    private final URI baseUrl;
    private final Set<String> metadataPrefixes;
    private final Path path;

    private final OaiGranularity granularity;

    private void harvest(OaiRequest.ListIdentifiers initialRequest, HarvestTrack track) {
      queue(initialRequest);

      while (!Thread.interrupted() && !queue.isEmpty()) {
        OaiResponse.Body.ListIdentifiers responseBody = executeListIdentifiers(queue.remove());
        if (responseBody != null) {
          handleListIdentifiers(responseBody);
          updateStatus(track, responseBody);
        }
      }
    }

    private void updateStatus(HarvestTrack track, OaiResponse.Body.ListIdentifiers responseBody) {
      TrackStatus status = buildStatus(responseBody.resumptionToken());
      statusService.update(track, status, path);
    }

    private TrackStatus buildStatus(@Nullable OaiResumptionToken resumptionToken) {
      if (resumptionToken == null) return TrackStatus.Done.INSTANCE;

      return new TrackStatus.InProgress(resumptionToken.content());
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
        return fakeEmptyResponseOnNoRecordsMatchError(e);
      }
    }

    private OaiResponse.Body.@Nullable ListIdentifiers fakeEmptyResponseOnNoRecordsMatchError(
        RequestService.OaiRequestError e) {
      boolean noRecordMatch =
          e.errors().stream().allMatch(error -> error.code() == OaiErrorCode.NO_RECORDS_MATCH);

      if (noRecordMatch) {
        log.atDebug().log("no records match, faking response");
        return new OaiResponse.Body.ListIdentifiers(List.of(), null);
      }

      log.atWarn().log("error executing list identifiers: {}", e.errors());

      return null;
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
