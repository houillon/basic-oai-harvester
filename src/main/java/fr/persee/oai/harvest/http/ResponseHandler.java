package fr.persee.oai.harvest.http;

import fr.persee.oai.domain.response.OaiResponse;
import fr.persee.oai.harvest.http.response.ResponseMapper;
import fr.persee.oai.harvest.http.response.ResponseParser;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import org.openarchives.oai._2.OAIPMHtype;

@RequiredArgsConstructor
public class ResponseHandler {
  private final ResponseParser parser;

  public OaiResponse handle(InputStream xml) {
    OAIPMHtype response = parser.parseResponse(xml);
    return ResponseMapper.mapResponse(response);
  }
}
