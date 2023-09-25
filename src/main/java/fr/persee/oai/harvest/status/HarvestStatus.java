package fr.persee.oai.harvest.status;

import static java.util.stream.Collectors.toMap;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fr.persee.oai.domain.request.OaiTimeBoundary;
import fr.persee.oai.harvest.status.json.HarvestTrackDeserializer;
import fr.persee.oai.harvest.status.json.HarvestTrackSerializer;
import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jspecify.annotations.Nullable;

public record HarvestStatus(
    Instant started,
    URI baseUrl,
    Set<String> metadataPrefixes,
    @JsonSerialize(keyUsing = HarvestTrackSerializer.class)
        @JsonDeserialize(keyUsing = HarvestTrackDeserializer.class)
        Map<HarvestTrack, TrackStatus> trackStatuses,
    @Nullable OaiTimeBoundary from,
    @Nullable OaiTimeBoundary until) {

  public static HarvestStatus forNewHarvest(
      Instant started,
      URI baseUrl,
      Set<String> metadataPrefixes,
      List<String> sets,
      @Nullable OaiTimeBoundary from,
      @Nullable OaiTimeBoundary until) {
    return new HarvestStatus(
        started, baseUrl, metadataPrefixes, buildInitialStatuses(sets), from, until);
  }

  private static Map<HarvestTrack, TrackStatus> buildInitialStatuses(List<String> sets) {
    if (sets.isEmpty()) {
      return Map.of(HarvestTrack.Full.INSTANCE, TrackStatus.Pending.INSTANCE);
    }

    return sets.stream().collect(toMap(HarvestTrack.Set::new, s -> TrackStatus.Pending.INSTANCE));
  }

  public HarvestStatus withTrackStatus(HarvestTrack track, TrackStatus status) {
    Map<HarvestTrack, TrackStatus> newSets = new HashMap<>(trackStatuses);
    newSets.put(track, status);
    return new HarvestStatus(started, baseUrl, metadataPrefixes, newSets, from, until);
  }
}
