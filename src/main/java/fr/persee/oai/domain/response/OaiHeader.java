package fr.persee.oai.domain.response;

import static java.util.stream.Collectors.toMap;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

public record OaiHeader(
    String identifier,
    Instant datestamp,
    List<String> setSpecs,
    OaiHeader.@Nullable Status status) {

  @RequiredArgsConstructor
  @Getter
  @SuppressWarnings("java:S6548")
  public enum Status {
    DELETED("deleted");

    private static final Map<String, Status> BY_VALUE =
        Arrays.stream(values()).collect(toMap(Status::value, Function.identity()));

    public static @Nullable Status getFromValue(String value) {
      return BY_VALUE.get(value);
    }

    private final String value;
  }
}
