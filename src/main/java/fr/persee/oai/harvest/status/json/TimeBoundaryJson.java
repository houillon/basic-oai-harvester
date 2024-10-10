package fr.persee.oai.harvest.status.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import fr.persee.oai.domain.OaiTimeBoundary;
import fr.persee.oai.harvest.http.response.TimeBoundaryMapping;
import java.io.IOException;

public class TimeBoundaryJson {
  public static class Serializer extends StdSerializer<OaiTimeBoundary> {
    public Serializer() {
      super(OaiTimeBoundary.class);
    }

    @Override
    public void serialize(OaiTimeBoundary value, JsonGenerator gen, SerializerProvider provider)
        throws IOException {
      gen.writeString(TimeBoundaryMapping.serialize(value));
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

      return TimeBoundaryMapping.parse(s);
    }
  }
}
