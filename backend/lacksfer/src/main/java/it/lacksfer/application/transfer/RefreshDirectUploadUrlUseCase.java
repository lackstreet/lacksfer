package it.lacksfer.application.transfer;

import it.lacksfer.application.transfer.result.StartDirectUploadResult;
import it.lacksfer.domain.exception.TransferExpiredException;
import it.lacksfer.domain.exception.TransferNotFoundException;
import it.lacksfer.domain.transfer.Transfer;
import it.lacksfer.ports.out.FileStoragePort;
import it.lacksfer.ports.out.TransferRepositoryPort;
import it.lacksfer.ports.out.UploadUrl;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class RefreshDirectUploadUrlUseCase {

    private final TransferRepositoryPort transferRepositoryPort;
    private final FileStoragePort fileStoragePort;


    public RefreshDirectUploadUrlUseCase(TransferRepositoryPort transferRepositoryPort, FileStoragePort fileStoragePort) {
        this.transferRepositoryPort = transferRepositoryPort;
        this.fileStoragePort = fileStoragePort;
    }

    public StartDirectUploadResult execute(UUID transferId){
        if (transferId == null) {
            throw new IllegalArgumentException("transferId is required");
        }

        Transfer transfer = transferRepositoryPort.findTransferById(transferId)
                .orElseThrow(
                () -> new TransferNotFoundException("Transfer not found")
        );

        if (transfer.isExpired()) {
            throw new TransferExpiredException("Transfer expired");
        }

        if (transfer.isReady()) {
            throw new IllegalStateException("Transfer is already ready");
        }

        UploadUrl uploadUrl = fileStoragePort.createUploadUrl(transfer.getBlobName());

        return new StartDirectUploadResult(
                transfer,
                uploadUrl.value(),
                uploadUrl.expiresAt().toString()
        );
    }
}
