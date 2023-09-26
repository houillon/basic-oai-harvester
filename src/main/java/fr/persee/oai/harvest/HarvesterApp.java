package fr.persee.oai.harvest;

import fr.persee.oai.harvest.command.HarvestCommand;
import fr.persee.oai.harvest.command.ResumeCommand;
import fr.persee.oai.harvest.command.UpdateCommand;
import fr.persee.oai.harvest.http.RequestService;
import fr.persee.oai.harvest.http.ResponseHandler;
import fr.persee.oai.harvest.http.response.ResponseParser;
import fr.persee.oai.harvest.status.StatusService;
import fr.persee.oai.harvest.status.json.StatusJsonSerialization;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import org.openarchives.oai._2.OAIPMHtype;
import picocli.CommandLine;

@CommandLine.Command(name = "basic-oai-harvester")
public class HarvesterApp {
  public static void main(String[] args) throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(OAIPMHtype.class);
    ResponseParser parser = new ResponseParser(jaxbContext);
    ResponseHandler responseHandler = new ResponseHandler(parser);
    RequestService requestService = new RequestService(responseHandler);

    StatusJsonSerialization statusJsonSerialization = new StatusJsonSerialization();
    StatusService statusService = new StatusService(statusJsonSerialization);

    Harvester harvester = new Harvester(requestService, statusService);

    HarvestCommand harvestCommand = new HarvestCommand(harvester);
    ResumeCommand resumeCommand = new ResumeCommand(harvester);
    UpdateCommand updateCommand = new UpdateCommand(harvester);

    new CommandLine(new HarvesterApp())
        .addSubcommand(harvestCommand)
        .addSubcommand(resumeCommand)
        .addSubcommand(updateCommand)
        .execute(args);
  }
}
