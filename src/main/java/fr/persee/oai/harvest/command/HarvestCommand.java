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
    name = "basic-oai-harvester",
    description = "Harvest OAI records from a repository, writing them to a directory",
    mixinStandardHelpOptions = true,
    sortOptions = false,
    sortSynopsis = false,
    usageHelpWidth = 120,
    usageHelpAutoWidth = true,
    versionProvider = HarvesterVersionProvider.class,
    footer = {
      "",
      "Examples:",
      "  basic-oai-harvester http://oai.persee.fr/oai --prefix=oai_dc --prefix=marc --set=persee:serie-geo:issue",
      "  basic-oai-harvester http://oai.persee.fr/oai --from=2020-01-01",
      "  basic-oai-harvester http://oai.persee.fr/oai --until=2020-01-01T23:59:59Z --dir=results"
    })
@RequiredArgsConstructor
@Slf4j
public class HarvestCommand implements Runnable {

  private final Harvester harvester;

  @CommandLine.Parameters(index = "0", description = "The OAI repository base url")
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
      log.atWarn().setCause(e).log("The path {} is not a directory or could not be created", path);
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
