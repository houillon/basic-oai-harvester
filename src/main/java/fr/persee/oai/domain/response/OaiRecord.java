package fr.persee.oai.domain.response;

import java.util.List;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Element;

public record OaiRecord(OaiHeader header, @Nullable Element metadata, List<Element> abouts) {}
