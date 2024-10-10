package fr.persee.oai.harvest.http;

import fr.persee.oai.domain.OaiTimeBoundary;
import fr.persee.oai.domain.request.OaiRequest;
import fr.persee.oai.domain.response.OaiGranularity;
import java.net.URI;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class RequestGenerator {

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
      case OaiRequest.Error __ ->
          throw new IllegalArgumentException("cannot build URI for error response request");
    };
  }

  private static URI buildUri(OaiRequest.GetRecord request) {
    return UriBuilder.fromUri(request.baseUrl())
        .queryParam(Param.VERB, Verb.GET_RECORD)
        .queryParam(Param.IDENTIFIER, request.identifier().toString())
        .queryParam(Param.METADATA_PREFIX, request.metadataPrefix())
        .build();
  }

  private static URI buildUri(OaiRequest.ListIdentifiers request, OaiGranularity granularity) {
    return switch (request) {
      case OaiRequest.ListIdentifiers.Initial i -> buildUri(i, granularity);
      case OaiRequest.ListIdentifiers.Resume r -> buildUri(r);
    };
  }

  private static URI buildUri(
      OaiRequest.ListIdentifiers.Initial request, OaiGranularity granularity) {
    return UriBuilder.fromUri(request.baseUrl())
        .queryParam(Param.VERB, Verb.LIST_IDENTIFIERS)
        .queryParamIfPresent(
            Param.FROM,
            Optional.ofNullable(request.from()).map(b -> mapTimeBoundary(b, granularity)))
        .queryParamIfPresent(
            Param.UNTIL,
            Optional.ofNullable(request.until()).map(b -> mapTimeBoundary(b, granularity)))
        .queryParamIfPresent(Param.SET, Optional.ofNullable(request.set()))
        .queryParam(Param.METADATA_PREFIX, request.metadataPrefix())
        .build();
  }

  private static URI buildUri(OaiRequest.ListIdentifiers.Resume request) {
    return UriBuilder.fromUri(request.baseUrl())
        .queryParam(Param.VERB, Verb.LIST_IDENTIFIERS)
        .queryParam(Param.RESUMPTION_TOKEN, request.resumptionToken())
        .build();
  }

  private static URI buildUri(OaiRequest.ListMetadataFormats request) {
    return switch (request) {
      case OaiRequest.ListMetadataFormats.All a -> buildUri(a);
      case OaiRequest.ListMetadataFormats.Item i -> buildUri(i);
    };
  }

  private static URI buildUri(OaiRequest.ListMetadataFormats.All request) {
    return UriBuilder.fromUri(request.baseUrl())
        .queryParam(Param.VERB, Verb.LIST_METADATA_FORMATS)
        .build();
  }

  private static URI buildUri(OaiRequest.ListMetadataFormats.Item request) {
    return UriBuilder.fromUri(request.baseUrl())
        .queryParam(Param.VERB, Verb.LIST_METADATA_FORMATS)
        .queryParam(Param.IDENTIFIER, request.identifier().toString())
        .build();
  }

  private static URI buildUri(OaiRequest.ListRecords request, OaiGranularity granularity) {
    return switch (request) {
      case OaiRequest.ListRecords.Initial i -> buildUri(i, granularity);
      case OaiRequest.ListRecords.Resume r -> buildUri(r);
    };
  }

  private static URI buildUri(OaiRequest.ListRecords.Initial request, OaiGranularity granularity) {
    return UriBuilder.fromUri(request.baseUrl())
        .queryParam(Param.VERB, Verb.LIST_RECORDS)
        .queryParamIfPresent(
            Param.FROM,
            Optional.ofNullable(request.from()).map(b -> mapTimeBoundary(b, granularity)))
        .queryParamIfPresent(
            Param.UNTIL,
            Optional.ofNullable(request.until()).map(b -> mapTimeBoundary(b, granularity)))
        .queryParamIfPresent(Param.SET, Optional.ofNullable(request.set()))
        .queryParam(Param.METADATA_PREFIX, request.metadataPrefix())
        .build();
  }

  private static URI buildUri(OaiRequest.ListRecords.Resume request) {
    return UriBuilder.fromUri(request.baseUrl())
        .queryParam(Param.VERB, Verb.LIST_RECORDS)
        .queryParam(Param.RESUMPTION_TOKEN, request.resumptionToken())
        .build();
  }

  private static URI buildUri(OaiRequest.ListSets request) {
    return switch (request) {
      case OaiRequest.ListSets.Initial i -> buildUri(i);
      case OaiRequest.ListSets.Resume r -> buildUri(r);
    };
  }

  private static URI buildUri(OaiRequest.ListSets.Initial request) {
    return UriBuilder.fromUri(request.baseUrl()).queryParam(Param.VERB, Verb.LIST_SETS).build();
  }

  private static URI buildUri(OaiRequest.ListSets.Resume request) {
    return UriBuilder.fromUri(request.baseUrl())
        .queryParam(Param.VERB, Verb.LIST_SETS)
        .queryParam(Param.RESUMPTION_TOKEN, request.resumptionToken())
        .build();
  }

  private static URI buildUri(OaiRequest.Identify request) {
    return UriBuilder.fromUri(request.baseUrl()).queryParam(Param.VERB, Verb.IDENTIFY).build();
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
      case OaiTimeBoundary.DateTime dt ->
          DateTimeFormatter.ISO_LOCAL_DATE.format(
              LocalDate.ofInstant(dt.instant(), ZoneOffset.UTC));
    };
  }

  private static String mapSecondGranularityTimeBoundary(OaiTimeBoundary boundary) {
    return switch (boundary) {
      case OaiTimeBoundary.DateTime dt -> DateTimeFormatter.ISO_INSTANT.format(dt.instant());
      case OaiTimeBoundary.Date __ ->
          throw new IllegalArgumentException("second granularity does not support date boundaries");
    };
  }
}
