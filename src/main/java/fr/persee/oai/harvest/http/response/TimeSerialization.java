package fr.persee.oai.harvest.http.response;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class TimeSerialization {
  private TimeSerialization() {}

  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_INSTANT;
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

  public static String serialize(Instant instant) {
    return DATE_TIME_FORMATTER.format(instant.truncatedTo(ChronoUnit.SECONDS));
  }

  public static String serialize(LocalDate localDate) {
    return DATE_FORMATTER.format(localDate);
  }

  public static Instant parseDateTime(String value) {
    return DATE_TIME_FORMATTER.parse(value, Instant::from);
  }

  public static LocalDate parseDate(String value) {
    return DATE_FORMATTER.parse(value, LocalDate::from);
  }
}
