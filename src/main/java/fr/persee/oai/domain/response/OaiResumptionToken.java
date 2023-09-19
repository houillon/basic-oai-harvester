package fr.persee.oai.domain.response;

import java.time.Instant;
import org.jspecify.annotations.Nullable;

public record OaiResumptionToken(
    String content,
    @Nullable Instant expirationDate,
    @Nullable Long completeListSize,
    @Nullable Long cursor) {}
