package fr.persee.oai.harvest.status;

import fr.persee.oai.harvest.status.json.StatusJsonSerialization;
import java.io.IOException;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

@RequiredArgsConstructor
@Slf4j
public class StatusService {
  private static final String STATUS_FILE = "harvest-status.json";

  private final StatusJsonSerialization serialization;

  public void write(HarvestStatus status, Path path) {
    try {
      serialization.writer().writeValue(path.resolve(STATUS_FILE).toFile(), status);
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
      return serialization
          .reader()
          .readValue(path.resolve(STATUS_FILE).toFile(), HarvestStatus.class);
    } catch (IOException e) {
      log.atError().setCause(e).log("error reading status file: {}", path);
      return null;
    }
  }
}
