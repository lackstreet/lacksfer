package it.lacksfer.adapters.in.rest.dto.response;

import it.lacksfer.domain.transfer.TransferStatus;

import java.util.UUID;

public record CompleteTransferUploadResponse (
        UUID transferId,
        String fileName,
        String downloadToken,
        TransferStatus status
) {
}
