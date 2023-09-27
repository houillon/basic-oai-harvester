package fr.persee.oai.harvest.command;

import fr.persee.oai.harvest.Harvester;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import picocli.CommandLine;

@CommandLine.Command(
    name = "resume",
    description = "Resume an interrupted harvest",
    sortOptions = false,
    sortSynopsis = false,
    usageHelpWidth = 120,
    usageHelpAutoWidth = true,
    header = {
      "The `resume` command resumes an interrupted harvest.",
      "",
      "The command requires an existing harvest directory, given by the --dir option, or uses the current directory if omitted.",
      "The command expects to find a harvest-status.json file in the harvest directory.",
      "Using the information in that file, it will resume the harvest from the point where it was last interrupted.",
      "The command can be used as many times as needed to complete a harvest that is interrupted multiple times.",
      "If the harvest is completed, the command will do nothing.",
      ""
    },
    footer = {"", "Examples:", "  basic-oai-harvester resume --dir=results"})
@RequiredArgsConstructor
public class ResumeCommand implements Runnable {

  private final Harvester harvester;

  @CommandLine.Option(
      names = {"--dir", "-d"},
      defaultValue = ".",
      description = "Directory of the interrupted harvest (default: current directory)")
  @SuppressWarnings("NullAway.Init")
  private Path path;

  @Override
  public void run() {
    harvester.resume(path);
  }
}
