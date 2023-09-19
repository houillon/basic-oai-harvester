package fr.persee.oai.domain.response;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

public record OaiHeader(
    URI identifier, Instant datestamp, List<String> setSpecs, OaiHeader.@Nullable Status status) {

  @RequiredArgsConstructor
  @Getter
  @SuppressWarnings("java:S6548")
  public enum Status {
    DELETED("deleted");

    private final String value;
  }
}
