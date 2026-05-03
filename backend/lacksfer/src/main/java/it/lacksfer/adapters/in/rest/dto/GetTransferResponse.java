package it.lacksfer.adapters.in.rest.dto;

import java.time.Instant;
import java.util.UUID;

public record GetTransferResponse (
        UUID id,
        String fileName,
        boolean expired
) {
}

