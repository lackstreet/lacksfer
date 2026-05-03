package it.lacksfer.adapters.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record CreateTransferRequest(
        @NotBlank(message = "fileName is required")
        String fileName,
        @NotNull(message = "expiresAt is required")
        Instant expiresAt
) {
}
