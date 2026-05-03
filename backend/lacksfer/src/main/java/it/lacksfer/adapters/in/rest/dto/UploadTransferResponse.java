package it.lacksfer.adapters.in.rest.dto;

import java.util.UUID;

public record UploadTransferResponse(
        UUID id,
        String fileName,
        String downloadToken
) {}

