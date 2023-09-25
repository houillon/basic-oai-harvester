package fr.persee.oai.harvest.status.json;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import fr.persee.oai.harvest.status.HarvestTrack;

public class HarvestTrackDeserializer extends KeyDeserializer {

  @Override
  public Object deserializeKey(String key, DeserializationContext ctxt) {
    return switch (key) {
      case HarvestTrackSerializer.FULL_SERIALIZED -> HarvestTrack.Full.INSTANCE;
      default -> new HarvestTrack.Set(key);
    };
  }
}
