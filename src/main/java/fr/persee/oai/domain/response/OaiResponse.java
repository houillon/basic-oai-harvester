package fr.persee.oai.domain.response;

import fr.persee.oai.domain.request.OaiRequest;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Element;

public record OaiResponse(Instant responseDate, OaiRequest request, Body body) {

  public sealed interface Body {
    record Identify(
        String repositoryName,
        URI baseURL,
        String protocolVersion,
        List<String> adminEmails,
        Instant earliestDatestamp,
        OaiDeletedRecord deletedRecord,
        OaiGranularity granularity,
        List<String> compressions,
        List<Element> descriptions)
        implements Body {}

    record ListMetadataFormats(List<OaiMetadataFormat> metadataFormats) implements Body {}

    record GetRecord(OaiRecord content) implements Body {}

    record ListSets(List<OaiSet> sets, @Nullable OaiResumptionToken resumptionToken)
        implements Paginated {}

    record ListIdentifiers(List<OaiHeader> headers, @Nullable OaiResumptionToken resumptionToken)
        implements Paginated {}

    record ListRecords(List<OaiRecord> records, @Nullable OaiResumptionToken resumptionToken)
        implements Paginated {}

    record Errors(List<OaiError> errors) implements Body {}

    sealed interface Paginated extends Body {
      @Nullable OaiResumptionToken resumptionToken();
    }
  }
}
