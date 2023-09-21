package fr.persee.oai.harvest;

import fr.persee.oai.domain.request.OaiTimeBoundary;
import fr.persee.oai.harvest.http.response.ResponseMapper;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class Runner implements ApplicationRunner {

  private final Harvester harvester;

  @Override
  public void run(ApplicationArguments args) {
    List<String> nonOptionArgs = args.getNonOptionArgs();

    if (nonOptionArgs.isEmpty() || args.containsOption("help")) {
      printHelp();
      return;
    }

    if (nonOptionArgs.size() > 1) {
      throw new IllegalArgumentException(
          "expected exactly one argument : the oai repository base url, found %s"
              .formatted(nonOptionArgs));
    }

    URI baseUrl = URI.create(nonOptionArgs.get(0));

    List<String> metadataPrefixes =
        Objects.requireNonNullElse(args.getOptionValues("prefix"), List.of());

    List<String> sets = Objects.requireNonNullElse(args.getOptionValues("set"), List.of());

    OaiTimeBoundary from = parseTimeBoundary("from", args);
    OaiTimeBoundary until = parseTimeBoundary("until", args);

    Path path = parsePath("dir", args);

    try {
      Files.createDirectories(path);
    } catch (IOException e) {
      log.atWarn().setCause(e).log("The path {} is not a directory or could not be created", path);
      return;
    }

    harvester.harvest(baseUrl, Set.copyOf(metadataPrefixes), Set.copyOf(sets), from, until, path);
  }

  private static @Nullable OaiTimeBoundary parseTimeBoundary(
      String name, ApplicationArguments args) {
    String value = getSingleArg(name, args);

    if (value == null) return null;

    return ResponseMapper.mapTimeBoundary(value);
  }

  private static Path parsePath(String name, ApplicationArguments args) {
    String value = getSingleArg(name, args);

    if (value == null) return Path.of(".");

    return Path.of(value);
  }

  private static @Nullable String getSingleArg(String name, ApplicationArguments args) {
    List<String> values = Objects.requireNonNullElse(args.getOptionValues(name), List.of());

    if (values.isEmpty()) return null;
    if (values.size() > 1)
      throw new IllegalArgumentException("expected at most one %s argument".formatted(name));

    return values.get(0);
  }

  private void printHelp() {
    String helpMessage =
        """
        Description:
          Harvests OAI records from a repository and writes them to files.

        Usage: java -jar basic-oai-harvest.jar <base-url> [--prefix=<prefix>]... [--set=<set>]... [--from=<from>] [--until=<until>] [--dir=<destination>] [--help]

        Options:
          --prefix=<prefix>  metadata prefix to harvest, repeatable (default: oai_dc)
          --set=<set>        set to harvest, repeatable (default: all sets)
          --from=<from>      harvest records from this date (inclusive)
          --until=<until>    harvest records until this date (inclusive)
          --dir=<dir>        directory to write records to (default: current directory)
          --help             print this help message

        Examples:
          java -jar basic-oai-harvest.jar http://oai.persee.fr/oai --prefix=oai_dc --set=persee:serie-geo:issue
          java -jar basic-oai-harvest.jar http://oai.persee.fr/oai --prefix=oai_dc --from=2020-01-01
          java -jar basic-oai-harvest.jar http://oai.persee.fr/oai --prefix=oai_dc --from=2020-01-01 --until=2020-12-31
        """;

    log.atInfo().log(helpMessage);
  }
}
