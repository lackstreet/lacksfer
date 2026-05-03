package it.lacksfer.adapters.in.rest.dto;

import java.util.UUID;

public record CreateTransferResponse (
        UUID id,
        String fileName,
        String downloadToken
) {
}
