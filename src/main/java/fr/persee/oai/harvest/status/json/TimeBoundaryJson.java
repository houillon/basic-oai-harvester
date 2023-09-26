package fr.persee.oai.harvest.status.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import fr.persee.oai.domain.request.OaiTimeBoundary;
import fr.persee.oai.harvest.http.response.ResponseMapper;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class TimeBoundaryJson {
  public static class Serializer extends StdSerializer<OaiTimeBoundary> {
    public Serializer() {
      super(OaiTimeBoundary.class);
    }

    @Override
    public void serialize(OaiTimeBoundary value, JsonGenerator gen, SerializerProvider provider)
        throws IOException {
      gen.writeString(serialize(value));
    }

    private String serialize(OaiTimeBoundary value) {
      return switch (value) {
        case OaiTimeBoundary.DateTime dt -> DateTimeFormatter.ISO_INSTANT.format(
            dt.instant().truncatedTo(ChronoUnit.SECONDS));
        case OaiTimeBoundary.Date d -> DateTimeFormatter.ISO_LOCAL_DATE.format(d.date());
      };
    }
  }

  public static class Deserializer extends StdDeserializer<OaiTimeBoundary> {
    public Deserializer() {
      super(OaiTimeBoundary.class);
    }

    @Override
    public OaiTimeBoundary deserialize(JsonParser p, DeserializationContext ctxt)
        throws IOException {
      String s = p.getCodec().readValue(p, String.class);

      return ResponseMapper.mapTimeBoundary(s);
    }
  }
}
