package fr.persee.oai.harvest.status.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import fr.persee.oai.harvest.status.HarvestTrack;
import java.io.IOException;

public class HarvestTrackJson {
  private static final String FULL_SERIALIZED = "@";

  public static class Serializer extends JsonSerializer<HarvestTrack> {

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

  public static class Deserializer extends KeyDeserializer {

    @Override
    public Object deserializeKey(String key, DeserializationContext ctxt) {
      return switch (key) {
        case FULL_SERIALIZED -> HarvestTrack.Full.INSTANCE;
        default -> new HarvestTrack.Set(key);
      };
    }
  }
}
