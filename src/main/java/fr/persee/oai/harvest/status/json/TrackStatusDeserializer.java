package fr.persee.oai.harvest.status.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import fr.persee.oai.harvest.status.TrackStatus;
import java.io.IOException;

public class TrackStatusDeserializer extends StdDeserializer<TrackStatus> {

  public TrackStatusDeserializer() {
    super(TrackStatus.class);
  }

  @Override
  public TrackStatus deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    String s = p.getCodec().readValue(p, String.class);

    return switch (s) {
      case TrackStatus.Pending.SERIALIZED -> TrackStatus.Pending.INSTANCE;
      case TrackStatus.Done.SERIALIZED -> TrackStatus.Done.INSTANCE;
      default -> {
        if (s.startsWith(TrackStatus.InProgress.PREFIX)) {
          yield new TrackStatus.InProgress(s.substring(TrackStatus.InProgress.PREFIX.length()));
        }

        throw new IllegalArgumentException("Unknown track status type: " + s);
      }
    };
  }
}
