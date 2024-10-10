package fr.persee.oai.harvest.http.response;

import fr.persee.oai.domain.OaiTimeBoundary;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

public class TimeBoundaryMapping {
  private TimeBoundaryMapping() {}

  public static String serialize(OaiTimeBoundary b) {
    return switch (b) {
      case OaiTimeBoundary.Date(LocalDate date) -> TimeSerialization.serialize(date);
      case OaiTimeBoundary.DateTime(Instant instant) -> TimeSerialization.serialize(instant);
    };
  }

  public static Instant mapFromToInstant(OaiTimeBoundary b) {
    return switch (b) {
      case OaiTimeBoundary.Date(LocalDate date) -> date.atStartOfDay().toInstant(ZoneOffset.UTC);
      case OaiTimeBoundary.DateTime(Instant instant) -> instant;
    };
  }

  public static OaiTimeBoundary parse(String value) {
    if (value.indexOf('T') > 1) {
      return new OaiTimeBoundary.DateTime(TimeSerialization.parseDateTime(value));
    } else {
      return new OaiTimeBoundary.Date(TimeSerialization.parseDate(value));
    }
  }
}
