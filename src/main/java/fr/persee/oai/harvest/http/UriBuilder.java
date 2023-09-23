package fr.persee.oai.harvest.http;

import static java.util.stream.Collectors.joining;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

@RequiredArgsConstructor
class UriBuilder {
  private final URI base;
  private final Map<String, String> params = new HashMap<>();

  public static UriBuilder fromUri(URI base) {
    return new UriBuilder(base);
  }

  public UriBuilder queryParam(String name, @Nullable String value) {
    if (value != null) params.put(name, value);

    return this;
  }

  public UriBuilder queryParamIfPresent(String name, Optional<String> value) {
    value.ifPresent(v -> params.put(name, v));

    return this;
  }

  public URI build() {
    String query = buildQuery();

    return URI.create(base.toString() + "?" + query);
  }

  private String buildQuery() {
    return params.entrySet().stream()
        .map(e -> encode(e.getKey()) + "=" + encode(e.getValue()))
        .collect(joining("&"));
  }

  private String encode(String s) {
    return URLEncoder.encode(s, StandardCharsets.UTF_8);
  }
}
