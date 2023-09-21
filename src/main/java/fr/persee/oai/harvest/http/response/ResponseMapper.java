package fr.persee.oai.harvest.http.response;

import fr.persee.oai.domain.request.OaiRequest;
import fr.persee.oai.domain.request.OaiTimeBoundary;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.function.Function;
import javax.xml.datatype.XMLGregorianCalendar;
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
import org.w3c.dom.Element;

public class ResponseMapper {

  public static OaiResponse mapResponse(OAIPMHtype response) {
    Instant responseDate = mapDate(response.getResponseDate());

    List<OAIPMHerrorType> error = response.getError();
    if (!error.isEmpty()) {
      return mapErrorResponse(error, responseDate, response.getRequest());
    }
    GetRecordType getRecord = response.getGetRecord();
    if (getRecord != null) {
      return mapGetRecordResponse(getRecord, responseDate, response.getRequest());
    }
    IdentifyType identify = response.getIdentify();
    if (identify != null) {
      return mapIdentifyResponse(identify, responseDate, response.getRequest());
    }
    ListIdentifiersType listIdentifiers = response.getListIdentifiers();
    if (listIdentifiers != null) {
      return mapListIdentifiersResponse(listIdentifiers, responseDate, response.getRequest());
    }
    ListMetadataFormatsType listMetadataFormats = response.getListMetadataFormats();
    if (listMetadataFormats != null) {
      return mapListMetadataFormatsResponse(
          listMetadataFormats, responseDate, response.getRequest());
    }
    ListRecordsType listRecords = response.getListRecords();
    if (listRecords != null) {
      return mapListRecordsResponse(listRecords, responseDate, response.getRequest());
    }
    ListSetsType listSets = response.getListSets();
    if (listSets != null) {
      return mapListSetsResponse(listSets, responseDate, response.getRequest());
    }

    throw new IllegalArgumentException("Unknown response type");
  }

  private static Instant mapDate(XMLGregorianCalendar date) {
    return date.toGregorianCalendar().toInstant();
  }

  private static OaiResponse mapErrorResponse(
      List<OAIPMHerrorType> errorElements, Instant responseDate, RequestType request) {
    return new OaiResponse(
        responseDate,
        new OaiRequest.ErrorResponseRequest(mapUri(request.getValue())),
        mapErrorBody(errorElements));
  }

  private static URI mapUri(String uri) {
    try {
      return new URI(uri);
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
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

  private static OaiResponse mapGetRecordResponse(
      GetRecordType getRecordElement, Instant responseDate, RequestType request) {
    return new OaiResponse(
        responseDate,
        new OaiRequest.GetRecord(
            mapUri(request.getValue()), request.getIdentifier(), request.getMetadataPrefix()),
        mapGetResponseBody(getRecordElement));
  }

  private static OaiResponse.Body.GetRecord mapGetResponseBody(GetRecordType getRecordElement) {
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
        header.getIdentifier(),
        DateTimeFormatter.ISO_INSTANT.parse(header.getDatestamp(), Instant::from),
        header.getSetSpec(),
        mapNullable(header.getStatus(), ResponseMapper::mapStatus));
  }

  private static OaiHeader.Status mapStatus(StatusType statusElement) {
    OaiHeader.Status status = OaiHeader.Status.getFromValue(statusElement.value());

    if (status == null) {
      throw new IllegalArgumentException("Unknown status value");
    }

    return status;
  }

  private static OaiResponse mapIdentifyResponse(
      IdentifyType identifyElement, Instant responseDate, RequestType request) {
    return new OaiResponse(
        responseDate,
        new OaiRequest.Identify(mapUri(request.getValue())),
        mapIdentifyBody(identifyElement));
  }

  private static OaiResponse.Body.Identify mapIdentifyBody(IdentifyType identifyElement) {
    return new OaiResponse.Body.Identify(
        identifyElement.getRepositoryName(),
        mapUri(identifyElement.getBaseURL()),
        identifyElement.getProtocolVersion(),
        identifyElement.getAdminEmail(),
        DateTimeFormatter.ISO_INSTANT.parse(identifyElement.getEarliestDatestamp(), Instant::from),
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

  private static OaiResponse mapListIdentifiersResponse(
      ListIdentifiersType listIdentifiersElement, Instant responseDate, RequestType request) {
    return new OaiResponse(
        responseDate,
        mapListIdentifiersRequest(request),
        mapListIdentifiersBody(listIdentifiersElement));
  }

  private static OaiRequest mapListIdentifiersRequest(RequestType request) {
    return new OaiRequest.ListIdentifiers(
        mapUri(request.getValue()),
        request.getMetadataPrefix(),
        request.getSet(),
        mapNullable(request.getFrom(), ResponseMapper::mapTimeBoundary),
        mapNullable(request.getUntil(), ResponseMapper::mapTimeBoundary),
        request.getResumptionToken());
  }

  public static OaiTimeBoundary mapTimeBoundary(String string) {
    try {
      LocalDate localDate = DateTimeFormatter.ISO_LOCAL_DATE.parse(string, LocalDate::from);
      return new OaiTimeBoundary.Date(localDate);
    } catch (DateTimeParseException e) {
      Instant instant = DateTimeFormatter.ISO_INSTANT.parse(string, Instant::from);
      return new OaiTimeBoundary.DateTime(instant);
    }
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
        mapNullable(resumptionTokenElement.getExpirationDate(), ResponseMapper::mapDate),
        mapNullable(resumptionTokenElement.getCompleteListSize(), BigInteger::longValue),
        mapNullable(resumptionTokenElement.getCursor(), BigInteger::longValue));
  }

  private static OaiResponse mapListMetadataFormatsResponse(
      ListMetadataFormatsType listMetadataFormatsElement,
      Instant responseDate,
      RequestType request) {
    return new OaiResponse(
        responseDate,
        new OaiRequest.ListMetadataFormats(mapUri(request.getValue()), request.getIdentifier()),
        mapListMetadataFormatsBody(listMetadataFormatsElement));
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

  private static OaiResponse mapListRecordsResponse(
      ListRecordsType listRecordsElement, Instant responseDate, RequestType request) {
    return new OaiResponse(
        responseDate, mapListRecordsRequest(request), mapListRecordsBody(listRecordsElement));
  }

  private static OaiRequest mapListRecordsRequest(RequestType request) {
    return new OaiRequest.ListRecords(
        mapUri(request.getValue()),
        request.getMetadataPrefix(),
        request.getSet(),
        mapNullable(request.getFrom(), ResponseMapper::mapTimeBoundary),
        mapNullable(request.getUntil(), ResponseMapper::mapTimeBoundary),
        request.getResumptionToken());
  }

  private static OaiResponse.Body.ListRecords mapListRecordsBody(
      ListRecordsType listRecordsElement) {
    return new OaiResponse.Body.ListRecords(
        listRecordsElement.getRecord().stream().map(ResponseMapper::mapRecord).toList(),
        mapNullable(listRecordsElement.getResumptionToken(), ResponseMapper::mapResumptionToken));
  }

  private static OaiResponse mapListSetsResponse(
      ListSetsType listSetsElement, Instant responseDate, RequestType request) {
    return new OaiResponse(
        responseDate,
        new OaiRequest.ListSets(mapUri(request.getValue()), request.getResumptionToken()),
        mapListSetsBody(listSetsElement));
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
}
