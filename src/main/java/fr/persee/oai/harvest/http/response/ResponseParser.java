package fr.persee.oai.harvest.http.response;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import java.io.InputStream;
import javax.xml.transform.stream.StreamSource;
import lombok.RequiredArgsConstructor;
import org.openarchives.oai._2.OAIPMHtype;

@RequiredArgsConstructor
public class ResponseParser {

  private final JAXBContext jaxbContext;

  public OAIPMHtype parseResponse(InputStream xml) {

    try {
      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
      return unmarshaller.unmarshal(new StreamSource(xml), OAIPMHtype.class).getValue();
    } catch (JAXBException e) {
      throw new RuntimeException(e);
    }
  }
}
