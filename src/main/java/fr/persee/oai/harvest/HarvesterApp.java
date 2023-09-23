package fr.persee.oai.harvest;

import fr.persee.oai.harvest.command.HarvestCommand;
import fr.persee.oai.harvest.http.RequestService;
import fr.persee.oai.harvest.http.ResponseHandler;
import fr.persee.oai.harvest.http.response.ResponseParser;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import org.openarchives.oai._2.OAIPMHtype;
import picocli.CommandLine;

public class HarvesterApp {
  public static void main(String[] args) throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(OAIPMHtype.class);
    ResponseParser parser = new ResponseParser(jaxbContext);
    ResponseHandler responseHandler = new ResponseHandler(parser);
    RequestService requestService = new RequestService(responseHandler);
    Harvester harvester = new Harvester(requestService);

    HarvestCommand harvestCommand = new HarvestCommand(harvester);

    new CommandLine(harvestCommand).execute(args);
  }
}
