package fr.persee.oai.harvest.command;

import fr.persee.oai.harvest.Harvester;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import picocli.CommandLine;

@CommandLine.Command(
    name = "update",
    description = "Update a previous harvest",
    mixinStandardHelpOptions = true,
    sortOptions = false,
    sortSynopsis = false,
    usageHelpWidth = 120,
    usageHelpAutoWidth = true,
    versionProvider = HarvesterVersionProvider.class,
    footer = {"", "Examples:", "  basic-oai-harvester resume --dir=results"})
@RequiredArgsConstructor
public class UpdateCommand implements Runnable {

  private final Harvester harvester;

  @CommandLine.Option(
      names = {"--dir", "-d"},
      defaultValue = ".",
      description = "Directory of the previous harvest (default: current directory)")
  @SuppressWarnings("NullAway.Init")
  private Path path;

  @Override
  public void run() {
    harvester.update(path);
  }
}
