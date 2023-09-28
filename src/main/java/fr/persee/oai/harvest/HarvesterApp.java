package fr.persee.oai.harvest;

import fr.persee.oai.harvest.command.HarvestCommand;
import fr.persee.oai.harvest.command.HarvesterVersionProvider;
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

@CommandLine.Command(
    name = "basic-oai-harvester",
    synopsisSubcommandLabel = "<COMMAND>",
    usageHelpWidth = 120,
    usageHelpAutoWidth = true,
    versionProvider = HarvesterVersionProvider.class,
    header = {
      "This is a program to harvest data from an OAI-PMH server and save the results to the filesystem.",
      "",
      "The program can be used in three different ways:",
      "  1. Start a new harvest",
      "  2. Resume an interrupted harvest",
      "  3. Update a completed harvest",
      "",
      "Use the `help` command to get more information about each command:",
      "  basic-oai-harvester help <COMMAND>",
      ""
    })
public class HarvesterApp implements Runnable {
  @SuppressWarnings("NullAway.Init")
  @CommandLine.Spec
  CommandLine.Model.CommandSpec spec;

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
        .addSubcommand(new CommandLine.HelpCommand())
        .execute(args);
  }

  @CommandLine.Option(
      names = {"-V", "--version"},
      versionHelp = true,
      description = "Print version information and exit.")
  private boolean versionRequested;

  @Override
  public void run() {
    spec.commandLine().usage(System.out);
  }
}
