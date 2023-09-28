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
public enum OaiErrorCode {
  BAD_ARGUMENT("badArgument"),
  BAD_RESUMPTION_TOKEN("badResumptionToken"),
  BAD_VERB("badVerb"),
  CANNOT_DISSEMINATE_FORMAT("cannotDisseminateFormat"),
  ID_DOES_NOT_EXIST("idDoesNotExist"),
  NO_RECORDS_MATCH("noRecordsMatch"),
  NO_METADATA_FORMATS("noMetadataFormats"),
  NO_SET_HIERARCHY("noSetHierarchy");

  private static final Map<String, OaiErrorCode> BY_VALUE =
      Arrays.stream(values()).collect(toMap(OaiErrorCode::value, Function.identity()));

  public static @Nullable OaiErrorCode getFromValue(String value) {
    return BY_VALUE.get(value);
  }

  private final String value;
}
