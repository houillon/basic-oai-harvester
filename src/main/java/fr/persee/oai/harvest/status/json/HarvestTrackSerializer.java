package fr.persee.oai.harvest.status.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import fr.persee.oai.harvest.status.HarvestTrack;
import java.io.IOException;

public class HarvestTrackSerializer extends JsonSerializer<HarvestTrack> {

  public static final String FULL_SERIALIZED = "@";

  @Override
  public void serialize(HarvestTrack value, JsonGenerator gen, SerializerProvider serializers)
      throws IOException {
    gen.writeFieldName(serialize(value));
  }

  private String serialize(HarvestTrack value) {
    return switch (value) {
      case HarvestTrack.Full __ -> FULL_SERIALIZED;
      case HarvestTrack.Set set -> set.name();
    };
  }
}
