package fr.persee.oai.harvest.status.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.module.SimpleModule;
import fr.persee.oai.domain.request.OaiTimeBoundary;
import fr.persee.oai.harvest.status.HarvestTrack;
import fr.persee.oai.harvest.status.TrackStatus;
import lombok.Getter;

@Getter
public class StatusJsonSerialization {

  private final ObjectWriter writer;
  private final ObjectReader reader;

  public StatusJsonSerialization() {
    ObjectMapper jsonMapper = new ObjectMapper();

    SimpleModule module = new SimpleModule();
    module.addKeySerializer(HarvestTrack.class, new HarvestTrackJson.Serializer());
    module.addKeyDeserializer(HarvestTrack.class, new HarvestTrackJson.Deserializer());
    module.addSerializer(TrackStatus.class, new TrackStatusJson.Serializer());
    module.addDeserializer(TrackStatus.class, new TrackStatusJson.Deserializer());
    module.addSerializer(OaiTimeBoundary.class, new TimeBoundaryJson.Serializer());
    module.addDeserializer(OaiTimeBoundary.class, new TimeBoundaryJson.Deserializer());

    jsonMapper.registerModule(module);

    writer = jsonMapper.writerWithDefaultPrettyPrinter();
    reader = jsonMapper.reader();
  }
}
