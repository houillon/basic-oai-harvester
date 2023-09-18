package fr.persee.oai.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum OaiGranularity {
  DAY("YYYY-MM-DD"),
  SECOND("YYYY-MM-DDThh:mm:ssZ");

  private final String value;
}
