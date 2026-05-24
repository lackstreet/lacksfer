package it.lacksfer.adapters.in.rest.dto.request;

import java.time.Instant;

public record StartDirectUploadRequest(
        String fileName,
        Instant expiresAt
) {}
