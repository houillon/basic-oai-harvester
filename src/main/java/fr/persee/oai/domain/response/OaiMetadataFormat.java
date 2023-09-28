package fr.persee.oai.domain.response;

import java.net.URI;

public record OaiMetadataFormat(String metadataPrefix, URI schema, URI metadataNamespace) {}
