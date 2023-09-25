package fr.persee.oai.harvest.status;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.WRAPPER_OBJECT)
public sealed interface HarvestTrack {
  record Set(String name) implements HarvestTrack {}

  record Full() implements HarvestTrack {
    public static final Full INSTANCE = new Full();
  }
}
