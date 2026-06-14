package it.lacksfer.application.transfer.result;

import java.util.List;
import java.util.UUID;

public record GetUploadedBlocksResult(
        UUID transferId,
        List<Integer> uploadedBlockIndexes
) {
}
