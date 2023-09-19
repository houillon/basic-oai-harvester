package fr.persee.oai.harvest.http;

import dev.failsafe.ExecutionContext;
import dev.failsafe.Failsafe;
import dev.failsafe.FailsafeException;
import dev.failsafe.RetryPolicy;
import fr.persee.oai.domain.request.OaiRequest;
import fr.persee.oai.domain.response.OaiGranularity;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.openarchives.oai._2.OAIPMHtype;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
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
    HttpStatus status = HttpStatus.resolve(response.statusCode());

    boolean shouldRetry = status == null || !status.is2xxSuccessful();

    if (shouldRetry) {
      response.body().close();
    }

    return shouldRetry;
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
    String retryAfter = response.headers().firstValue(HttpHeaders.RETRY_AFTER).orElse(null);
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

  private final OaiHttpResponseParser parser;

  public OAIPMHtype execute(OaiRequest request, OaiGranularity granularity) {
    URI uri = OaiHttpRequestGenerator.uri(request, granularity);

    HttpRequest httpRequest =
        HttpRequest.newBuilder(uri).GET().timeout(Duration.ofMinutes(1)).build();

    try {
      HttpResponse<InputStream> response =
          Failsafe.with(retryPolicy)
              .get(() -> httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream()));

      try (InputStream is = response.body()) {
        return parser.parse(is);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (FailsafeException e) {
      throw new RuntimeException(e.getCause());
    }
  }
}
