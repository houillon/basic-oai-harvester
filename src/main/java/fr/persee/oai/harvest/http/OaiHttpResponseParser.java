package fr.persee.oai.harvest.http;

import java.io.InputStream;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import lombok.RequiredArgsConstructor;
import org.openarchives.oai._2.OAIPMHtype;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OaiHttpResponseParser {

  private final ObjectProvider<Unmarshaller> unmarshallerProvider;

  public OAIPMHtype parse(InputStream xml) {
    Unmarshaller unmarshaller = unmarshallerProvider.getObject();

    try {
      return unmarshaller.unmarshal(new StreamSource(xml), OAIPMHtype.class).getValue();
    } catch (JAXBException e) {
      throw new RuntimeException(e);
    }
  }
}
