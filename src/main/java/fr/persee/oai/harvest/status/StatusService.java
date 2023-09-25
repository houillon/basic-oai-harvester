package fr.persee.oai.harvest.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.persee.oai.domain.request.OaiTimeBoundary;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

@Slf4j
public class StatusService {
  private static final String STATUS_FILE = "harvest-status.json";

  public static void main(String[] args) throws JsonProcessingException {
    StatusService statusService = new StatusService();

    HarvestStatus original =
        HarvestStatus.forNewHarvest(
            Instant.parse("2021-09-01T00:00:00Z"),
            URI.create("https://www.persee.fr/oai"),
            Set.of("oai_dc"),
            List.of(),
            new OaiTimeBoundary.Date(LocalDate.now()),
            new OaiTimeBoundary.DateTime(Instant.now()));

    String s = statusService.jsonMapper.writeValueAsString(original);

    System.out.println(s);

    HarvestStatus harvestStatus = statusService.jsonMapper.readValue(s, HarvestStatus.class);

    System.out.println(harvestStatus);
  }

  private final ObjectMapper jsonMapper;

  public StatusService() {
    jsonMapper = new ObjectMapper();
    jsonMapper.registerModule(new JavaTimeModule());
    jsonMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }

  public void write(HarvestStatus status, Path path) {
    try {
      jsonMapper.writeValue(path.resolve(STATUS_FILE).toFile(), status);
    } catch (IOException e) {
      log.atError().setCause(e).log("error writing status file: {}", path);
    }
  }

  public void update(HarvestTrack track, TrackStatus status, Path path) {
    HarvestStatus harvestStatus = read(path);
    if (harvestStatus == null) {
      log.atError().log("no status file found: {}", path);
      return;
    }

    HarvestStatus newStatus = harvestStatus.withTrackStatus(track, status);
    write(newStatus, path);
  }

  public @Nullable HarvestStatus read(Path path) {
    try {
      return jsonMapper.readValue(path.resolve(STATUS_FILE).toFile(), HarvestStatus.class);
    } catch (IOException e) {
      log.atError().setCause(e).log("error reading status file: {}", path);
      return null;
    }
  }
}
