package it.lacksfer.ports.out;

import java.time.Instant;

public record UploadUrl(
        String value,
        Instant expiresAt
) {}
