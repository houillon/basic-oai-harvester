package fr.persee.oai.harvest.status.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import fr.persee.oai.harvest.status.TrackStatus;
import java.io.IOException;

public class TrackStatusJson {
  public static final String PENDING_SERIALIZED = "@pending";
  public static final String IN_PROGRESS_PREFIX = "@in-progress:";
  public static final String DONE_SERIALIZED = "@done";

  public static class Serializer extends StdSerializer<TrackStatus> {
    public Serializer() {
      super(TrackStatus.class);
    }

    @Override
    public void serialize(TrackStatus value, JsonGenerator gen, SerializerProvider provider)
        throws IOException {
      gen.writeString(serialize(value));
    }

    private String serialize(TrackStatus value) {
      return switch (value) {
        case TrackStatus.Pending __ -> PENDING_SERIALIZED;
        case TrackStatus.Done __ -> DONE_SERIALIZED;
        case TrackStatus.InProgress inProgress -> IN_PROGRESS_PREFIX + inProgress.resumptionToken();
      };
    }
  }

  public static class Deserializer extends StdDeserializer<TrackStatus> {
    public Deserializer() {
      super(TrackStatus.class);
    }

    @Override
    public TrackStatus deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
      String s = p.getCodec().readValue(p, String.class);

      return switch (s) {
        case PENDING_SERIALIZED -> TrackStatus.Pending.INSTANCE;
        case DONE_SERIALIZED -> TrackStatus.Done.INSTANCE;
        default -> {
          if (s.startsWith(IN_PROGRESS_PREFIX)) {
            yield new TrackStatus.InProgress(s.substring(IN_PROGRESS_PREFIX.length()));
          }

          throw new IllegalArgumentException("Unknown track status type: " + s);
        }
      };
    }
  }
}
