package fr.persee.oai.harvest.http.response;

import fr.persee.oai.domain.OaiTimeBoundary;
import fr.persee.oai.domain.request.OaiRequest;
import fr.persee.oai.domain.response.OaiDeletedRecord;
import fr.persee.oai.domain.response.OaiError;
import fr.persee.oai.domain.response.OaiErrorCode;
import fr.persee.oai.domain.response.OaiGranularity;
import fr.persee.oai.domain.response.OaiHeader;
import fr.persee.oai.domain.response.OaiMetadataFormat;
import fr.persee.oai.domain.response.OaiRecord;
import fr.persee.oai.domain.response.OaiResponse;
import fr.persee.oai.domain.response.OaiResumptionToken;
import fr.persee.oai.domain.response.OaiSet;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;
import org.openarchives.oai._2.AboutType;
import org.openarchives.oai._2.DeletedRecordType;
import org.openarchives.oai._2.DescriptionType;
import org.openarchives.oai._2.GetRecordType;
import org.openarchives.oai._2.GranularityType;
import org.openarchives.oai._2.HeaderType;
import org.openarchives.oai._2.IdentifyType;
import org.openarchives.oai._2.ListIdentifiersType;
import org.openarchives.oai._2.ListMetadataFormatsType;
import org.openarchives.oai._2.ListRecordsType;
import org.openarchives.oai._2.ListSetsType;
import org.openarchives.oai._2.MetadataFormatType;
import org.openarchives.oai._2.OAIPMHerrorType;
import org.openarchives.oai._2.OAIPMHerrorcodeType;
import org.openarchives.oai._2.OAIPMHtype;
import org.openarchives.oai._2.RecordType;
import org.openarchives.oai._2.RequestType;
import org.openarchives.oai._2.ResumptionTokenType;
import org.openarchives.oai._2.SetType;
import org.openarchives.oai._2.StatusType;
import org.openarchives.oai._2.VerbType;
import org.w3c.dom.Element;

public class ResponseMapper {

  public static OaiResponse mapResponse(OAIPMHtype response) {
    Instant responseDate = XmlCalendarMapping.toInstant(response.getResponseDate());
    OaiRequest request = mapRequest(response.getRequest());
    OaiResponse.Body body = mapBody(response, request);

    return new OaiResponse(responseDate, request, body);
  }

  private static OaiRequest mapRequest(RequestType request) {
    VerbType verb = request.getVerb();

    return switch (verb) {
      case GET_RECORD -> mapGetRecordRequest(request);
      case IDENTIFY -> mapIdentifyRequest(request);
      case LIST_IDENTIFIERS -> mapListIdentifiersRequest(request);
      case LIST_METADATA_FORMATS -> mapListMetadataFormatsRequest(request);
      case LIST_RECORDS -> mapListRecordsRequest(request);
      case LIST_SETS -> mapListSetsRequest(request);
      case null -> mapErrorRequest(request);
    };
  }

  private static OaiRequest.GetRecord mapGetRecordRequest(RequestType request) {
    return new OaiRequest.GetRecord(
        mapUri(request.getValue()), mapUri(request.getIdentifier()), request.getMetadataPrefix());
  }

  private static OaiRequest.Identify mapIdentifyRequest(RequestType request) {
    return new OaiRequest.Identify(mapUri(request.getValue()));
  }

  private static OaiRequest mapListIdentifiersRequest(RequestType request) {
    URI baseUrl = mapUri(request.getValue());
    String resumptionToken = request.getResumptionToken();

    if (resumptionToken != null) {
      return new OaiRequest.ListIdentifiers.Resume(baseUrl, resumptionToken);
    }

    return new OaiRequest.ListIdentifiers.Initial(
        baseUrl,
        request.getMetadataPrefix(),
        request.getSet(),
        mapNullable(request.getFrom(), TimeBoundaryMapping::parse),
        mapNullable(request.getUntil(), TimeBoundaryMapping::parse));
  }

  private static OaiRequest.ListMetadataFormats mapListMetadataFormatsRequest(RequestType request) {
    URI baseUrl = mapUri(request.getValue());
    String identifier = request.getIdentifier();

    if (identifier != null) {
      return new OaiRequest.ListMetadataFormats.Item(baseUrl, mapUri(identifier));
    }

    return new OaiRequest.ListMetadataFormats.All(baseUrl);
  }

  private static OaiRequest mapListRecordsRequest(RequestType request) {
    URI baseUrl = mapUri(request.getValue());
    String resumptionToken = request.getResumptionToken();

    if (resumptionToken != null) {
      return new OaiRequest.ListRecords.Resume(baseUrl, resumptionToken);
    }

    return new OaiRequest.ListRecords.Initial(
        baseUrl,
        request.getMetadataPrefix(),
        request.getSet(),
        mapNullable(request.getFrom(), TimeBoundaryMapping::parse),
        mapNullable(request.getUntil(), TimeBoundaryMapping::parse));
  }

  private static OaiRequest.ListSets mapListSetsRequest(RequestType request) {
    URI baseUrl = mapUri(request.getValue());
    String resumptionToken = request.getResumptionToken();

    if (resumptionToken != null) {
      return new OaiRequest.ListSets.Resume(baseUrl, resumptionToken);
    }

    return new OaiRequest.ListSets.Initial(baseUrl);
  }

  private static OaiRequest.Error mapErrorRequest(RequestType request) {
    return new OaiRequest.Error(mapUri(request.getValue()));
  }

  private static OaiResponse.Body mapBody(OAIPMHtype response, OaiRequest request) {
    return switch (request) {
      case OaiRequest.GetRecord __ -> mapGetRecordBody(response.getGetRecord());
      case OaiRequest.Identify __ -> mapIdentifyBody(response.getIdentify());
      case OaiRequest.ListIdentifiers __ -> mapListIdentifiersBody(response.getListIdentifiers());
      case OaiRequest.ListMetadataFormats __ ->
          mapListMetadataFormatsBody(response.getListMetadataFormats());
      case OaiRequest.ListRecords __ -> mapListRecordsBody(response.getListRecords());
      case OaiRequest.ListSets __ -> mapListSetsBody(response.getListSets());
      case OaiRequest.Error __ -> mapErrorBody(response.getError());
    };
  }

  private static OaiResponse.Body.GetRecord mapGetRecordBody(GetRecordType getRecordElement) {
    return new OaiResponse.Body.GetRecord(mapRecord(getRecordElement.getRecord()));
  }

  private static OaiRecord mapRecord(RecordType recordElement) {
    return new OaiRecord(
        mapHeader(recordElement.getHeader()),
        mapNullable(recordElement.getMetadata(), m -> mapElement(m.getAny())),
        recordElement.getAbout().stream()
            .map(AboutType::getAny)
            .map(ResponseMapper::mapElement)
            .toList());
  }

  private static Element mapElement(Object any) {
    return (Element) any;
  }

  private static OaiHeader mapHeader(HeaderType header) {
    return new OaiHeader(
        mapUri(header.getIdentifier()),
        parseFromBoundary(header.getDatestamp()),
        header.getSetSpec(),
        mapNullable(header.getStatus(), ResponseMapper::mapStatus));
  }

  private static Instant parseFromBoundary(String header) {
    OaiTimeBoundary datestampBoundary = TimeBoundaryMapping.parse(header);
    return TimeBoundaryMapping.mapFromToInstant(datestampBoundary);
  }

  private static OaiHeader.Status mapStatus(StatusType statusElement) {
    OaiHeader.Status status = OaiHeader.Status.getFromValue(statusElement.value());

    if (status == null) {
      throw new IllegalArgumentException("Unknown status value");
    }

    return status;
  }

  private static OaiResponse.Body.Identify mapIdentifyBody(IdentifyType identifyElement) {
    return new OaiResponse.Body.Identify(
        identifyElement.getRepositoryName(),
        mapUri(identifyElement.getBaseURL()),
        identifyElement.getProtocolVersion(),
        identifyElement.getAdminEmail(),
        parseFromBoundary(identifyElement.getEarliestDatestamp()),
        mapDeletedRecord(identifyElement.getDeletedRecord()),
        mapGranularity(identifyElement.getGranularity()),
        identifyElement.getCompression(),
        identifyElement.getDescription().stream()
            .map(DescriptionType::getAny)
            .map(ResponseMapper::mapElement)
            .toList());
  }

  private static OaiDeletedRecord mapDeletedRecord(DeletedRecordType deletedRecordElement) {
    OaiDeletedRecord deletedRecord = OaiDeletedRecord.getFromValue(deletedRecordElement.value());

    if (deletedRecord == null) throw new IllegalArgumentException("Unknown deleted record value");

    return deletedRecord;
  }

  private static OaiGranularity mapGranularity(GranularityType granularityElement) {
    OaiGranularity granularity = OaiGranularity.getFromValue(granularityElement.value());

    if (granularity == null) throw new IllegalArgumentException("Unknown granularity value");

    return granularity;
  }

  private static <@Nullable T, R> @Nullable R mapNullable(@Nullable T t, Function<T, R> mapper) {
    return t == null ? null : mapper.apply(t);
  }

  private static OaiResponse.Body.ListIdentifiers mapListIdentifiersBody(
      ListIdentifiersType listIdentifiersElement) {
    return new OaiResponse.Body.ListIdentifiers(
        listIdentifiersElement.getHeader().stream().map(ResponseMapper::mapHeader).toList(),
        mapNullable(
            listIdentifiersElement.getResumptionToken(), ResponseMapper::mapResumptionToken));
  }

  private static OaiResumptionToken mapResumptionToken(ResumptionTokenType resumptionTokenElement) {
    return new OaiResumptionToken(
        resumptionTokenElement.getValue(),
        mapNullable(resumptionTokenElement.getExpirationDate(), XmlCalendarMapping::toInstant),
        mapNullable(resumptionTokenElement.getCompleteListSize(), BigInteger::longValue),
        mapNullable(resumptionTokenElement.getCursor(), BigInteger::longValue));
  }

  private static OaiResponse.Body.ListMetadataFormats mapListMetadataFormatsBody(
      ListMetadataFormatsType listMetadataFormatsElement) {
    return new OaiResponse.Body.ListMetadataFormats(
        listMetadataFormatsElement.getMetadataFormat().stream()
            .map(ResponseMapper::mapMetadataFormat)
            .toList());
  }

  private static OaiMetadataFormat mapMetadataFormat(MetadataFormatType metadataFormat) {
    return new OaiMetadataFormat(
        metadataFormat.getMetadataPrefix(),
        mapUri(metadataFormat.getSchema()),
        mapUri(metadataFormat.getMetadataNamespace()));
  }

  private static OaiResponse.Body.ListRecords mapListRecordsBody(
      ListRecordsType listRecordsElement) {
    return new OaiResponse.Body.ListRecords(
        listRecordsElement.getRecord().stream().map(ResponseMapper::mapRecord).toList(),
        mapNullable(listRecordsElement.getResumptionToken(), ResponseMapper::mapResumptionToken));
  }

  private static OaiResponse.Body.ListSets mapListSetsBody(ListSetsType listSetsElement) {
    return new OaiResponse.Body.ListSets(
        listSetsElement.getSet().stream().map(ResponseMapper::mapSet).toList(),
        mapNullable(listSetsElement.getResumptionToken(), ResponseMapper::mapResumptionToken));
  }

  private static OaiSet mapSet(SetType set) {
    return new OaiSet(
        set.getSetSpec(),
        set.getSetName(),
        set.getSetDescription().stream()
            .map(DescriptionType::getAny)
            .map(ResponseMapper::mapElement)
            .toList());
  }

  private static OaiResponse.Body.Errors mapErrorBody(List<OAIPMHerrorType> errorElements) {
    List<OaiError> errors =
        errorElements.stream()
            .map(e -> new OaiError(e.getValue(), mapErrorCode(e.getCode())))
            .toList();

    return new OaiResponse.Body.Errors(errors);
  }

  private static OaiErrorCode mapErrorCode(OAIPMHerrorcodeType code) {
    OaiErrorCode errorCode = OaiErrorCode.getFromValue(code.value());

    if (errorCode == null) {
      throw new IllegalArgumentException("Unknown error code");
    }

    return errorCode;
  }

  private static URI mapUri(String uri) {
    try {
      return new URI(uri);
    } catch (URISyntaxException e) {
      throw new RuntimeException(
          String.format("can't map string '%s' to uri : %s", uri, e.getMessage()), e);
    }
  }
}
