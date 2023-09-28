package fr.persee.oai.domain.request;

import java.net.URI;
import org.jspecify.annotations.Nullable;

public sealed interface OaiRequest {

  URI baseUrl();

  record GetRecord(URI baseUrl, String identifier, String metadataPrefix) implements OaiRequest {}

  record ListIdentifiers(
      URI baseUrl,
      @Nullable String metadataPrefix,
      @Nullable String set,
      @Nullable OaiTimeBoundary from,
      @Nullable OaiTimeBoundary until,
      @Nullable String resumptionToken)
      implements OaiRequest {
    public static ListIdentifiers of(
        URI baseUrl,
        String metadataPrefix,
        @Nullable String set,
        @Nullable OaiTimeBoundary from,
        @Nullable OaiTimeBoundary until) {
      return new ListIdentifiers(baseUrl, metadataPrefix, set, from, until, null);
    }

    public static ListIdentifiers of(URI baseUrl, String resumptionToken) {
      return new ListIdentifiers(baseUrl, null, null, null, null, resumptionToken);
    }
  }

  record ListMetadataFormats(URI baseUrl, @Nullable String identifier) implements OaiRequest {}

  record ListRecords(
      URI baseUrl,
      @Nullable String metadataPrefix,
      @Nullable String set,
      @Nullable OaiTimeBoundary from,
      @Nullable OaiTimeBoundary until,
      @Nullable String resumptionToken)
      implements OaiRequest {}

  record ListSets(URI baseUrl, @Nullable String resumptionToken) implements OaiRequest {}

  record Identify(URI baseUrl) implements OaiRequest {}

  record ErrorResponseRequest(URI baseUrl) implements OaiRequest {}
}
