package fr.persee.oai.domain.response;

import static java.util.stream.Collectors.toMap;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

@RequiredArgsConstructor
@Getter
public enum OaiDeletedRecord {
  NO("no"),
  PERSISTENT("persistent"),
  TRANSIENT("transient");

  private static final Map<String, OaiDeletedRecord> BY_VALUE =
      Arrays.stream(values()).collect(toMap(OaiDeletedRecord::value, Function.identity()));

  public static @Nullable OaiDeletedRecord getFromValue(String value) {
    return BY_VALUE.get(value);
  }

  private final String value;
}
