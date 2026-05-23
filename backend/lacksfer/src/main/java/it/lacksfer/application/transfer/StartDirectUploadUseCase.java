package it.lacksfer.application.transfer;

import it.lacksfer.application.transfer.result.StartDirectUploadResult;
import it.lacksfer.domain.transfer.Transfer;
import it.lacksfer.ports.out.FileStoragePort;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;

@ApplicationScoped
public class StartDirectUploadUseCase {
    private final CreatePendingTransferUseCase createPendingTransferUseCase;
    private final FileStoragePort fileStoragePort;

    public StartDirectUploadUseCase(CreatePendingTransferUseCase createPendingTransferUseCase, FileStoragePort fileStoragePort) {
        this.createPendingTransferUseCase = createPendingTransferUseCase;
        this.fileStoragePort = fileStoragePort;
    }

    public StartDirectUploadResult execute(String fileName, Instant expiresAt, String blobName) {
        Transfer transfer = createPendingTransferUseCase.execute(fileName, expiresAt, blobName);
        String uploadUrl = fileStoragePort.createUploadUrl(transfer.getBlobName());

        return new StartDirectUploadResult(transfer, uploadUrl);
    }
}
