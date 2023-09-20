package fr.persee.oai.harvest;

import fr.persee.oai.domain.response.OaiGranularity;
import fr.persee.oai.harvest.http.RequestService;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Runner implements ApplicationRunner {

  private final RequestService requestService;

  @Override
  public void run(ApplicationArguments args) {
    URI baseUrl = URI.create("http://oai.persee.fr/oai");

    new Harvester("oai_dc", baseUrl, requestService, OaiGranularity.SECOND).harvest();
  }
}
