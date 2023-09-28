package fr.persee.oai.harvest.command;

import fr.persee.oai.domain.request.OaiTimeBoundary;
import fr.persee.oai.harvest.Harvester;
import fr.persee.oai.harvest.http.response.ResponseMapper;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import picocli.CommandLine;

@CommandLine.Command(
    name = "harvest",
    description = "Start a new harvest",
    sortOptions = false,
    sortSynopsis = false,
    usageHelpWidth = 120,
    usageHelpAutoWidth = true,
    header = {
      "The `harvest` command starts a new harvest.",
      "",
      "The harvested data will be written to the directory specified by the --dir option, or the current directory if omitted.",
      "Each record will be written in a directory named after the record identifier, and the record will be written to a file named after the metadata prefix.",
      "Additionally, a harvest-status.json file will be created in the harvest directory, which contains the status of the harvest. If one exists from a previous harvest, it will be overwritten.",
      "This file is used to resume an interrupted harvest, or to update a completed harvest, with the corresponding command. It can be safely deleted if the harvest is completed and you don't expect you will update the data.",
      ""
    },
    footer = {
      "",
      "Examples:",
      "  basic-oai-harvester harvest http://oai.server.com",
      "  basic-oai-harvester harvest http://oai.server.com --prefix=oai_dc --prefix=marc --set=server.com:collection",
      "  basic-oai-harvester harvest http://oai.server.com --from=2020-01-01",
      "  basic-oai-harvester harvest http://oai.server.com --until=2020-01-01T23:59:59Z --dir=results"
    })
@RequiredArgsConstructor
@Slf4j
public class HarvestCommand implements Runnable {

  private final Harvester harvester;

  @CommandLine.Parameters(index = "0", description = "The OAI-PMH repository base url")
  @SuppressWarnings("NullAway.Init")
  private URI baseUrl;

  @CommandLine.Option(
      names = {"--prefix", "-p"},
      defaultValue = "oai_dc",
      description = "Metadata prefix(es) to harvest, repeatable (default: ${DEFAULT-VALUE})",
      paramLabel = "<prefix>")
  private final Set<String> metadataPrefixes = new HashSet<>();

  @CommandLine.Option(
      names = {"--set", "-s"},
      description = "Set(s) to harvest, repeatable (default: no set specified)",
      paramLabel = "<set>")
  private final List<String> sets = new ArrayList<>();

  @CommandLine.Option(
      names = {"--from", "-f"},
      description =
          "Harvest records from this date, should be in the granularity of the repository, as given by the Identify response")
  private @Nullable String from;

  @CommandLine.Option(
      names = {"--until", "-u"},
      description =
          "Harvest records until this date, should be in the granularity of the repository, as given by the Identify response")
  private @Nullable String until;

  @CommandLine.Option(
      names = {"--dir", "-d"},
      defaultValue = ".",
      description = "Directory to write records to (default: current directory)")
  @SuppressWarnings("NullAway.Init")
  private Path path;

  @Override
  public void run() {
    OaiTimeBoundary fromBoundary =
        Optional.ofNullable(from).map(ResponseMapper::mapTimeBoundary).orElse(null);
    OaiTimeBoundary untilBoundary =
        Optional.ofNullable(until).map(ResponseMapper::mapTimeBoundary).orElse(null);

    try {
      Files.createDirectories(path);
    } catch (IOException e) {
      log.atWarn()
          .setCause(e)
          .log("The path {} is not a directory or could not be created", path.toAbsolutePath());
      return;
    }

    log.atDebug().log(
        "baseUrl: {}, metadataPrefixes: {}, sets: {}, from: {}, until: {}, path: {}",
        baseUrl,
        metadataPrefixes,
        sets,
        fromBoundary,
        untilBoundary,
        path);

    harvester.harvest(baseUrl, metadataPrefixes, sets, fromBoundary, untilBoundary, path);
  }
}
