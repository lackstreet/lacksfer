package it.lacksfer.adapters.in.rest.dto.response;

import java.util.List;
import java.util.UUID;

public record GetUploadedBlocksResponse(
        UUID transferId,
        List<Integer> uploadedBlockIndexes
) {
}
