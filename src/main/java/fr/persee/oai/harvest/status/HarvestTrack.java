package fr.persee.oai.harvest.status;

public sealed interface HarvestTrack {
  record Set(String name) implements HarvestTrack {}

  record Full() implements HarvestTrack {
    public static final Full INSTANCE = new Full();
  }
}
