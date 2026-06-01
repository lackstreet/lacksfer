package it.lacksfer.application.transfer;

import it.lacksfer.domain.exception.TransferNotFoundException;
import it.lacksfer.domain.transfer.Transfer;
import it.lacksfer.ports.out.FileStoragePort;
import it.lacksfer.ports.out.StorageFileMetadata;
import it.lacksfer.ports.out.TransferRepositoryPort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class CompleteTransferUploadUseCase {
    private final TransferRepositoryPort transferRepositoryPort;
    private final FileStoragePort fileStoragePort;

    public CompleteTransferUploadUseCase(TransferRepositoryPort transferRepositoryPort, FileStoragePort fileStoragePort) {
        this.transferRepositoryPort = transferRepositoryPort;
        this.fileStoragePort = fileStoragePort;
    }

    public Transfer execute(UUID transferId) {
        if (transferId == null) {
            throw new IllegalArgumentException("transferId is required");
        }
        Transfer transfer = transferRepositoryPort.findTransferById(transferId).orElseThrow(
                () -> new TransferNotFoundException("Transfer not found")
        );

        StorageFileMetadata metadata = fileStoragePort.getMetadata(transfer.getBlobName());


        if (!metadata.exists()) {
            throw new IllegalStateException("Uploaded file was not found");
        }

        if (metadata.sizeBytes() != transfer.getExpectedStorageSizeBytes()) {
            throw new IllegalStateException("Uploaded file size does not match expected size");
        }

        transfer.markAsReady();
        return transferRepositoryPort.save(transfer);
    }
}
