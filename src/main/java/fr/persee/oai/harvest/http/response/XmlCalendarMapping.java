package fr.persee.oai.harvest.http.response;

import java.time.Instant;
import javax.xml.datatype.XMLGregorianCalendar;

public class XmlCalendarMapping {
  private XmlCalendarMapping() {}

  public static Instant toInstant(XMLGregorianCalendar date) {
    return date.toGregorianCalendar().toInstant();
  }
}
