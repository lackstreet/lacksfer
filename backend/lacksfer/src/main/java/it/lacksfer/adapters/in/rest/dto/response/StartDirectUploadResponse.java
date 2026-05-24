package it.lacksfer.adapters.in.rest.dto.response;

import java.util.UUID;

public record StartDirectUploadResponse(
        UUID transferId,
        String fileName,
        String downloadToken,
        String uploadUrl
) {
}
