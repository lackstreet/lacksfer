package it.lacksfer.application.transfer;
import it.lacksfer.domain.exception.TransferExpiredException;
import it.lacksfer.domain.exception.TransferNotFoundException;
import it.lacksfer.domain.transfer.Transfer;
import it.lacksfer.ports.out.FileStoragePort;
import it.lacksfer.ports.out.TransferRepositoryPort;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.InputStream;
@ApplicationScoped

public class DownloadTransferUseCase {
    private final TransferRepositoryPort repositoryPort;
    private final FileStoragePort fileStoragePort;

    public DownloadTransferUseCase(
            TransferRepositoryPort repositoryPort,
            FileStoragePort fileStoragePort
    ){
        this.repositoryPort = repositoryPort;
        this.fileStoragePort = fileStoragePort;
    }

    public DownloadTransferResult execute(String downloadToken){
        if(downloadToken == null || downloadToken.isBlank()){
            throw new IllegalArgumentException("downloadToken is required");
        }


        Transfer transfer = repositoryPort.findByDownloadToken(downloadToken)
                .orElseThrow(() -> new TransferNotFoundException("Transfer not found"));

        if (transfer.isExpired()) {
            throw new TransferExpiredException("Transfer expired");
        }

        InputStream content = fileStoragePort.download(transfer.getBlobName());

        return new DownloadTransferResult(transfer.getFileName(),content);

    }
}
