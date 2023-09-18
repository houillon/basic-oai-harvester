package fr.persee.oai.request;

import org.jspecify.annotations.Nullable;

public sealed interface OaiRequest {
  record GetRecord(String identifier, String metadataPrefix) implements OaiRequest {}

  record ListIdentifiers(
      @Nullable String metadataPrefix,
      @Nullable String set,
      @Nullable String from,
      @Nullable String until,
      @Nullable String resumptionToken)
      implements OaiRequest {}

  record ListMetadataFormats(@Nullable String identifier) implements OaiRequest {}

  record ListRecords(
      @Nullable String metadataPrefix,
      @Nullable String set,
      @Nullable String from,
      @Nullable String until,
      @Nullable String resumptionToken)
      implements OaiRequest {}

  record ListSets(@Nullable String resumptionToken) implements OaiRequest {}

  record Identify() implements OaiRequest {}
}
