package fr.persee.oai.harvest.http;

import fr.persee.oai.domain.request.OaiRequest;
import fr.persee.oai.domain.request.OaiTimeBoundary;
import fr.persee.oai.domain.response.OaiGranularity;
import java.net.URI;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilderFactory;

public class RequestGenerator {

  private static final UriBuilderFactory UBF = new DefaultUriBuilderFactory();

  private static class Verb {
    public static final String IDENTIFY = "Identify";
    public static final String LIST_METADATA_FORMATS = "ListMetadataFormats";
    public static final String LIST_SETS = "ListSets";
    public static final String GET_RECORD = "GetRecord";
    public static final String LIST_IDENTIFIERS = "ListIdentifiers";
    public static final String LIST_RECORDS = "ListRecords";
  }

  private static class Param {
    public static final String VERB = "verb";
    public static final String IDENTIFIER = "identifier";
    public static final String METADATA_PREFIX = "metadataPrefix";
    public static final String FROM = "from";
    public static final String UNTIL = "until";
    public static final String SET = "set";
    public static final String RESUMPTION_TOKEN = "resumptionToken";
  }

  public static URI uri(OaiRequest request, OaiGranularity granularity) {
    return switch (request) {
      case OaiRequest.GetRecord r -> buildUri(r);
      case OaiRequest.ListIdentifiers r -> buildUri(r, granularity);
      case OaiRequest.ListMetadataFormats r -> buildUri(r);
      case OaiRequest.ListRecords r -> buildUri(r, granularity);
      case OaiRequest.ListSets r -> buildUri(r);
      case OaiRequest.Identify r -> buildUri(r);
      case OaiRequest.ErrorResponseRequest ignored -> throw new IllegalArgumentException(
          "cannot build URI for error response request");
    };
  }

  private static URI buildUri(OaiRequest.GetRecord request) {
    return UBF.uriString(request.baseUrl().toString())
        .queryParam(Param.VERB, Verb.GET_RECORD)
        .queryParam(Param.IDENTIFIER, request.identifier())
        .queryParam(Param.METADATA_PREFIX, request.metadataPrefix())
        .build();
  }

  private static URI buildUri(OaiRequest.ListIdentifiers request, OaiGranularity granularity) {
    return UBF.uriString(request.baseUrl().toString())
        .queryParam(Param.VERB, Verb.LIST_IDENTIFIERS)
        .queryParamIfPresent(
            Param.FROM,
            Optional.ofNullable(request.from()).map(b -> mapTimeBoundary(b, granularity)))
        .queryParamIfPresent(
            Param.UNTIL,
            Optional.ofNullable(request.until()).map(b -> mapTimeBoundary(b, granularity)))
        .queryParamIfPresent(Param.SET, Optional.ofNullable(request.set()))
        .queryParamIfPresent(Param.RESUMPTION_TOKEN, Optional.ofNullable(request.resumptionToken()))
        .queryParamIfPresent(Param.METADATA_PREFIX, Optional.ofNullable(request.metadataPrefix()))
        .build();
  }

  private static URI buildUri(OaiRequest.ListMetadataFormats request) {
    return UBF.uriString(request.baseUrl().toString())
        .queryParam(Param.VERB, Verb.LIST_METADATA_FORMATS)
        .queryParamIfPresent(Param.IDENTIFIER, Optional.ofNullable(request.identifier()))
        .build();
  }

  private static URI buildUri(OaiRequest.ListRecords request, OaiGranularity granularity) {
    return UBF.uriString(request.baseUrl().toString())
        .queryParam(Param.VERB, Verb.LIST_RECORDS)
        .queryParamIfPresent(
            Param.FROM,
            Optional.ofNullable(request.from()).map(b -> mapTimeBoundary(b, granularity)))
        .queryParamIfPresent(
            Param.UNTIL,
            Optional.ofNullable(request.until()).map(b -> mapTimeBoundary(b, granularity)))
        .queryParamIfPresent(Param.SET, Optional.ofNullable(request.set()))
        .queryParamIfPresent(Param.RESUMPTION_TOKEN, Optional.ofNullable(request.resumptionToken()))
        .queryParamIfPresent(Param.METADATA_PREFIX, Optional.ofNullable(request.metadataPrefix()))
        .build();
  }

  private static URI buildUri(OaiRequest.ListSets request) {
    return UBF.uriString(request.baseUrl().toString())
        .queryParam(Param.VERB, Verb.LIST_SETS)
        .queryParamIfPresent(Param.RESUMPTION_TOKEN, Optional.ofNullable(request.resumptionToken()))
        .build();
  }

  private static URI buildUri(OaiRequest.Identify request) {
    return UBF.uriString(request.baseUrl().toString())
        .queryParam(Param.VERB, Verb.IDENTIFY)
        .build();
  }

  private static String mapTimeBoundary(OaiTimeBoundary boundary, OaiGranularity granularity) {
    return switch (granularity) {
      case OaiGranularity.DAY -> mapDayGranularityTimeBoundary(boundary);
      case OaiGranularity.SECOND -> mapSecondGranularityTimeBoundary(boundary);
    };
  }

  private static String mapDayGranularityTimeBoundary(OaiTimeBoundary boundary) {
    return switch (boundary) {
      case OaiTimeBoundary.Date d -> DateTimeFormatter.ISO_LOCAL_DATE.format(d.date());
      case OaiTimeBoundary.DateTime dt -> DateTimeFormatter.ISO_LOCAL_DATE.format(
          LocalDate.ofInstant(dt.instant(), ZoneOffset.UTC));
    };
  }

  private static String mapSecondGranularityTimeBoundary(OaiTimeBoundary boundary) {
    return switch (boundary) {
      case OaiTimeBoundary.DateTime dt -> DateTimeFormatter.ISO_INSTANT.format(dt.instant());
      case OaiTimeBoundary.Date ignored -> throw new IllegalArgumentException(
          "second granularity does not support date boundaries");
    };
  }
}
