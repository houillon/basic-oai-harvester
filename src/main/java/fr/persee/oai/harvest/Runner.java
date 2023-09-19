package fr.persee.oai.harvest;

import fr.persee.oai.domain.request.OaiRequest;
import fr.persee.oai.domain.response.OaiGranularity;
import fr.persee.oai.harvest.http.RequestService;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.openarchives.oai._2.OAIPMHtype;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Runner implements ApplicationRunner {

  private final RequestService requestService;

  @Override
  public void run(ApplicationArguments args) {
    OaiRequest.GetRecord getRecord =
        new OaiRequest.GetRecord(
            URI.create("http://oai.persee.fr/oai"),
            "oai:persee:article/ahess_0395-2649_1992_num_47_1_279421",
            "oai_dc");

    OAIPMHtype response1 = requestService.execute(getRecord, OaiGranularity.DAY);

    System.out.println(response1);

    OaiRequest.Identify identify = new OaiRequest.Identify(URI.create("http://oai.persee.fr/oai"));

    OAIPMHtype response2 = requestService.execute(identify, OaiGranularity.DAY);

    System.out.println(response2);
  }
}
