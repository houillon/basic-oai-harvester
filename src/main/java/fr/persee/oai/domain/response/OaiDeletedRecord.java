package fr.persee.oai.domain.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum OaiDeletedRecord {
  NO("no"),
  PERSISTENT("persistent"),
  TRANSIENT("transient");

  private final String value;
}
