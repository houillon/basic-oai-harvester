package fr.persee.oai.domain.request;

import fr.persee.oai.domain.OaiTimeBoundary;
import java.net.URI;
import org.jspecify.annotations.Nullable;

public sealed interface OaiRequest {

  URI baseUrl();

  sealed interface Paginated extends OaiRequest {}

  sealed interface Filtered extends Paginated {}

  sealed interface Pagination extends OaiRequest {
    String resumptionToken();
  }

  sealed interface PaginationData extends OaiRequest {}

  sealed interface Filter extends PaginationData {
    String metadataPrefix();

    @Nullable String set();

    @Nullable OaiTimeBoundary from();

    @Nullable OaiTimeBoundary until();
  }

  record GetRecord(URI baseUrl, URI identifier, String metadataPrefix) implements OaiRequest {}

  sealed interface ListMetadataFormats extends OaiRequest {
    record All(URI baseUrl) implements ListMetadataFormats {}

    record Item(URI baseUrl, URI identifier) implements ListMetadataFormats {}
  }

  sealed interface ListSets extends Paginated {
    record Initial(URI baseUrl) implements ListSets, PaginationData {}

    record Resume(URI baseUrl, String resumptionToken) implements ListSets, Pagination {}
  }

  record Identify(URI baseUrl) implements OaiRequest {}

  record Error(URI baseUrl) implements OaiRequest {}

  sealed interface ListIdentifiers extends Filtered {
    record Initial(
        URI baseUrl,
        String metadataPrefix,
        @Nullable String set,
        @Nullable OaiTimeBoundary from,
        @Nullable OaiTimeBoundary until)
        implements ListIdentifiers, Filter {}

    record Resume(URI baseUrl, String resumptionToken) implements ListIdentifiers, Pagination {}
  }

  sealed interface ListRecords extends Filtered {
    record Initial(
        URI baseUrl,
        String metadataPrefix,
        @Nullable String set,
        @Nullable OaiTimeBoundary from,
        @Nullable OaiTimeBoundary until)
        implements ListRecords, Filter {}

    record Resume(URI baseUrl, String resumptionToken) implements ListRecords, Pagination {}
  }
}
