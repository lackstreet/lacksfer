package it.lacksfer.adapters.in.rest.dto;

import java.time.Instant;

public record CreateTransferRequest(
        String fileName,
        Instant expiresAt
) {
}
