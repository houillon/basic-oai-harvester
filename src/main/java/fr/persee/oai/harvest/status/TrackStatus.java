package fr.persee.oai.harvest.status;


public sealed interface TrackStatus {
  record Pending() implements TrackStatus {
    public static final Pending INSTANCE = new Pending();
  }

  record InProgress(String resumptionToken) implements TrackStatus {}

  record Done() implements TrackStatus {
    public static final Done INSTANCE = new Done();
  }
}
