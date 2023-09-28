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
public enum OaiGranularity {
  DAY("YYYY-MM-DD"),
  SECOND("YYYY-MM-DDThh:mm:ssZ");

  private static final Map<String, OaiGranularity> BY_VALUE =
      Arrays.stream(values()).collect(toMap(OaiGranularity::value, Function.identity()));

  public static @Nullable OaiGranularity getFromValue(String value) {
    return BY_VALUE.get(value);
  }

  private final String value;
}
