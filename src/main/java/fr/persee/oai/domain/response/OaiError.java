package fr.persee.oai.domain.response;

import org.jspecify.annotations.Nullable;

public record OaiError(@Nullable String content, OaiErrorCode code) {}
