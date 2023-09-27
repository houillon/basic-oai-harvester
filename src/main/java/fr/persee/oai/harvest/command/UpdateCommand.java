package fr.persee.oai.harvest.command;

import fr.persee.oai.harvest.Harvester;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import picocli.CommandLine;

@CommandLine.Command(
    name = "update",
    description = "Update a completed harvest",
    sortOptions = false,
    sortSynopsis = false,
    usageHelpWidth = 120,
    usageHelpAutoWidth = true,
    header = {
      "The `update` command updates a completed harvest.",
      "",
      "The command requires an existing harvest directory, given by the --dir option, or uses the current directory if omitted.",
      "The command expects to find a harvest-status.json file in the harvest directory.",
      "Using the information in that file, it will start a new harvest with a <from> parameter set to the date of the previous harvest, and using the same <baseUrl>, <set>, <prefix> and <until> configuration.",
      "Except for the fact that the options are set automatically, the started harvest is like a new harvest from the `harvest` command, so it can be resumed if interrupted, or updated again later if completed.",
      "If the harvest is not completed, the command will do nothing, use `resume` first.",
      ""
    },
    footer = {"", "Examples:", "  basic-oai-harvester resume --dir=results"})
@RequiredArgsConstructor
public class UpdateCommand implements Runnable {

  private final Harvester harvester;

  @CommandLine.Option(
      names = {"--dir", "-d"},
      defaultValue = ".",
      description = "Directory of a completed harvest (default: current directory)")
  @SuppressWarnings("NullAway.Init")
  private Path path;

  @Override
  public void run() {
    harvester.update(path);
  }
}
