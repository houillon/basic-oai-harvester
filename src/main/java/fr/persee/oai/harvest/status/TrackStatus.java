package fr.persee.oai.harvest.status;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fr.persee.oai.harvest.status.json.TrackStatusDeserializer;

@JsonDeserialize(using = TrackStatusDeserializer.class)
public sealed interface TrackStatus {
  record Pending() implements TrackStatus {
    public static final Pending INSTANCE = new Pending();
    public static final String SERIALIZED = "@pending";

    @JsonValue
    public String serialize() {
      return SERIALIZED;
    }
  }

  record InProgress(String resumptionToken) implements TrackStatus {
    public static final String PREFIX = "@in-progress:";

    @JsonValue
    public String serialize() {
      return PREFIX + resumptionToken;
    }
  }

  record Done() implements TrackStatus {
    public static final Done INSTANCE = new Done();
    public static final String SERIALIZED = "@done";

    @JsonValue
    public String serialize() {
      return SERIALIZED;
    }
  }
}
