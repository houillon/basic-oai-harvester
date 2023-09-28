package fr.persee.oai.domain.request;

import java.time.Instant;
import java.time.LocalDate;

public sealed interface OaiTimeBoundary {

  record Date(LocalDate date) implements OaiTimeBoundary {}

  record DateTime(Instant instant) implements OaiTimeBoundary {}
}
