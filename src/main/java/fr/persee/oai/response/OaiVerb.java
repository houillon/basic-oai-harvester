package fr.persee.oai.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum OaiVerb {
  IDENTIFY("Identify"),
  LIST_METADATA_FORMATS("ListMetadataFormats"),
  LIST_SETS("ListSets"),
  GET_RECORD("GetRecord"),
  LIST_IDENTIFIERS("ListIdentifiers"),
  LIST_RECORDS("ListRecords");

  private final String value;
}
