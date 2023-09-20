package fr.persee.oai.harvest;

import fr.persee.oai.domain.request.OaiRequest;
import fr.persee.oai.domain.response.OaiGranularity;
import fr.persee.oai.domain.response.OaiHeader;
import fr.persee.oai.domain.response.OaiResponse;
import fr.persee.oai.domain.response.OaiResumptionToken;
import fr.persee.oai.harvest.http.RequestService;
import java.net.URI;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class Harvester {

  private final BlockingQueue<QueueItem<OaiRequest>> queue = new ArrayBlockingQueue<>(1000);

  private final String metadataPrefix;
  private final URI baseUrl;
  private final RequestService requestService;
  private final OaiGranularity granularity;

  public void harvest() {
    queue(new OaiRequest.ListIdentifiers(baseUrl, metadataPrefix, null, null, null, null));

    while (!Thread.interrupted()) {
      try {
        QueueItem<OaiRequest> queueItem = queue.take();

        switch (queueItem) {
          case QueueItem.Item<OaiRequest> item -> harvest(item.item());
          case QueueItem.End<OaiRequest> ignored -> Thread.currentThread().interrupt();
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }

  private void harvest(OaiRequest request) {
    switch (request) {
      case OaiRequest.GetRecord getRecord -> harvest(getRecord);
      case OaiRequest.ListIdentifiers listIdentifiers -> harvest(listIdentifiers);
      default -> throw new IllegalStateException("unexpected request type: " + request);
    }
  }

  private void harvest(OaiRequest.GetRecord getRecord) {
    OaiResponse response = requestService.execute(getRecord, granularity);

    OaiResponse.Body body = response.body();

    if (!(body instanceof OaiResponse.Body.GetRecord getRecordBody)) {
      throw new IllegalStateException("unexpected body type: " + body);
    }

    handleGetRecord(getRecordBody);
  }

  private void handleGetRecord(OaiResponse.Body.GetRecord body) {
    log.atDebug().log("handling get record: {}", body);
  }

  private void harvest(OaiRequest.ListIdentifiers listIdentifiers) {
    OaiResponse response = requestService.execute(listIdentifiers, granularity);

    OaiResponse.Body body = response.body();

    if (!(body instanceof OaiResponse.Body.ListIdentifiers listIdentifiersBody)) {
      throw new IllegalStateException("unexpected body type: " + body);
    }

    handleListIdentifiers(listIdentifiersBody);
  }

  private void handleListIdentifiers(OaiResponse.Body.ListIdentifiers body) {
    body.headers().forEach(this::handleIdentifier);

    OaiResumptionToken resumptionToken = body.resumptionToken();
    if (resumptionToken != null) {
      handleResumptionToken(resumptionToken);
    }
  }

  private void handleIdentifier(OaiHeader oaiHeader) {
    log.atDebug().log("handling identifier: {}", oaiHeader.identifier());

    OaiRequest.GetRecord getRecord =
        new OaiRequest.GetRecord(baseUrl, oaiHeader.identifier(), metadataPrefix);

    queue(getRecord);
  }

  private void queue(OaiRequest request) {
    try {
      log.atDebug().log("queueing request: {}", request);
      queue.put(new QueueItem.Item<>(request));
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  private void handleResumptionToken(OaiResumptionToken resumptionToken) {
    log.atDebug().log("handling resumption token: {}", resumptionToken.content());

    OaiRequest.ListIdentifiers listIdentifiers =
        new OaiRequest.ListIdentifiers(baseUrl, null, null, null, null, resumptionToken.content());

    queue(listIdentifiers);
  }

  sealed interface QueueItem<T> {
    record Item<T>(T item) implements QueueItem<T> {}

    record End<T>() implements QueueItem<T> {}
  }
}
