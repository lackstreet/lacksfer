package it.lacksfer.adapters.in.rest.dto.response;

import java.util.UUID;

public record UploadTransferResponse(
        UUID id,
        String fileName,
        String downloadToken
) {}

