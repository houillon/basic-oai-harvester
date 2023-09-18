package fr.persee.oai.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

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

  private final String value;
}
