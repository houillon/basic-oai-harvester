package fr.persee.oai.harvest;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.openarchives.oai._2.OAIPMHtype;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

@SpringBootApplication
public class HarvesterApp {
  public static void main(String[] args) {
    new SpringApplicationBuilder(HarvesterApp.class).web(WebApplicationType.NONE).run(args);
  }

  @Bean
  public JAXBContext jaxbContext() throws JAXBException {
    return JAXBContext.newInstance(OAIPMHtype.class);
  }

  @Bean
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  public Unmarshaller unmarshaller(JAXBContext jaxbContext) throws JAXBException {
    return jaxbContext.createUnmarshaller();
  }
}
