package it.lacksfer.application.transfer;
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

    public InputStream execute(String downloadToken){

        if (downloadToken == null || downloadToken.isBlank()){
            throw new IllegalArgumentException("downloadToken is required");
        }

        Transfer transfer = repositoryPort.findByDownloadToken(downloadToken)
                .orElseThrow(() -> new IllegalArgumentException("Download token not valid"));

        if(transfer.isExpired()){
            throw new IllegalArgumentException("Download link expired");
        }

        return fileStoragePort.download(transfer.getBlobName());


    }
}
