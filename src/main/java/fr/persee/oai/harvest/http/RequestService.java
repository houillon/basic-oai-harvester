package fr.persee.oai.harvest.http;

import dev.failsafe.ExecutionContext;
import dev.failsafe.Failsafe;
import dev.failsafe.FailsafeException;
import dev.failsafe.RetryPolicy;
import fr.persee.oai.domain.request.OaiRequest;
import fr.persee.oai.domain.response.OaiError;
import fr.persee.oai.domain.response.OaiGranularity;
import fr.persee.oai.domain.response.OaiResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

@RequiredArgsConstructor
@Slf4j
public class RequestService {

  private final HttpClient httpClient =
      HttpClient.newBuilder()
          .followRedirects(HttpClient.Redirect.ALWAYS)
          .connectTimeout(Duration.ofSeconds(10))
          .build();

  private final RetryPolicy<HttpResponse<InputStream>> retryPolicy =
      RetryPolicy.<HttpResponse<InputStream>>builder()
          .handle(IOException.class)
          .handleResultIf(RequestService::shouldRetry)
          .withDelayFn(RequestService::computeRetryDelay)
          .withMaxRetries(3)
          .build();

  private static boolean shouldRetry(HttpResponse<InputStream> response) throws IOException {
    boolean shouldRetry = !is2xxSuccessful(response.statusCode());

    if (shouldRetry) {
      response.body().close();
    }

    return shouldRetry;
  }

  private static boolean is2xxSuccessful(int status) {
    return status >= 200 && status < 300;
  }

  private static Duration computeRetryDelay(ExecutionContext<HttpResponse<InputStream>> context) {
    HttpResponse<InputStream> lastResult = context.getLastResult();
    if (lastResult != null && lastResult.statusCode() == 503) {
      Duration retryAfterDelay = getRetryAfterDelay(lastResult);
      if (retryAfterDelay != null) return retryAfterDelay;
    }

    return Duration.ofSeconds(1);
  }

  private static @Nullable Duration getRetryAfterDelay(HttpResponse<InputStream> response) {
    String retryAfter = response.headers().firstValue("retry-after").orElse(null);
    if (retryAfter != null) {
      try {
        int seconds = Integer.parseInt(retryAfter);
        if (seconds > 0) return Duration.ofSeconds(seconds);
      } catch (NumberFormatException e) {
        Instant retryAt = DateTimeFormatter.RFC_1123_DATE_TIME.parse(retryAfter, Instant::from);
        return Duration.between(Instant.now(), retryAt);
      }
    }

    return null;
  }

  private final ResponseHandler responseHandler;

  public Timestamped<OaiResponse.Body.Identify> identify(OaiRequest.Identify request) {
    try {
      return execute(request, OaiGranularity.SECOND, OaiResponse.Body.Identify.class);
    } catch (OaiRequestError e) {
      throw new IllegalStateException("identify should not return errors", e);
    }
  }

  public OaiResponse.Body.ListMetadataFormats listMetadataFormats(
      OaiRequest.ListMetadataFormats request) throws OaiRequestError {
    return execute(request, OaiGranularity.SECOND, OaiResponse.Body.ListMetadataFormats.class)
        .value();
  }

  public OaiResponse.Body.ListSets listSets(OaiRequest.ListSets request) throws OaiRequestError {
    return execute(request, OaiGranularity.SECOND, OaiResponse.Body.ListSets.class).value();
  }

  public OaiResponse.Body.ListIdentifiers listIdentifiers(
      OaiRequest.ListIdentifiers request, OaiGranularity granularity) throws OaiRequestError {
    return execute(request, granularity, OaiResponse.Body.ListIdentifiers.class).value();
  }

  public OaiResponse.Body.ListRecords listRecords(
      OaiRequest.ListRecords request, OaiGranularity granularity) throws OaiRequestError {
    return execute(request, granularity, OaiResponse.Body.ListRecords.class).value();
  }

  public OaiResponse.Body.GetRecord getRecord(OaiRequest.GetRecord request) throws OaiRequestError {
    return execute(request, OaiGranularity.SECOND, OaiResponse.Body.GetRecord.class).value();
  }

  private <T extends OaiResponse.Body> Timestamped<T> execute(
      OaiRequest request, OaiGranularity granularity, Class<T> bodyClass) throws OaiRequestError {
    OaiResponse response = execute(request, granularity);

    OaiResponse.Body body = response.body();

    if (bodyClass.isInstance(body)) {
      return new Timestamped<>(bodyClass.cast(body), response.responseDate());
    }

    if (body instanceof OaiResponse.Body.Errors errors) {
      throw new OaiRequestError(errors.errors());
    }

    throw new IllegalStateException(
        String.format("unexpected body type: %s for request %s", body, request));
  }

  public record Timestamped<T>(T value, Instant timestamp) {}

  @RequiredArgsConstructor
  @Getter
  public static class OaiRequestError extends Exception {
    private final List<OaiError> errors;
  }

  public OaiResponse execute(OaiRequest request, OaiGranularity granularity) {
    log.atDebug().log("executing request: {}", request);

    URI uri = RequestGenerator.uri(request, granularity);

    HttpRequest httpRequest =
        HttpRequest.newBuilder(uri).GET().timeout(Duration.ofMinutes(1)).build();

    try {
      HttpResponse<InputStream> response =
          Failsafe.with(retryPolicy)
              .get(() -> httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream()));

      try (InputStream is = response.body()) {
        return responseHandler.handle(is);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (FailsafeException e) {
      throw new RuntimeException(e.getCause());
    }
  }
}
