package fr.persee.oai.response;

import java.util.List;
import org.w3c.dom.Element;

public record OaiSet(String spec, String name, List<Element> descriptions) {}
