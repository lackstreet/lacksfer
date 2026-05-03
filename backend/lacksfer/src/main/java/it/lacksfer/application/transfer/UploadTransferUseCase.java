package it.lacksfer.application.transfer;

import it.lacksfer.domain.file.FileContent;
import it.lacksfer.domain.transfer.Transfer;
import it.lacksfer.ports.out.FileStoragePort;
import it.lacksfer.ports.out.TransferRepositoryPort;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.time.Instant;

@ApplicationScoped
public class UploadTransferUseCase {

    private final FileStoragePort fileStoragePort;
    private final TransferRepositoryPort transferRepositoryPort;
    private static final Logger LOG = Logger.getLogger(UploadTransferUseCase.class);

    public UploadTransferUseCase(FileStoragePort fileStoragePort, TransferRepositoryPort transferRepositoryPort) {
        this.fileStoragePort = fileStoragePort;
        this.transferRepositoryPort = transferRepositoryPort;
    }

    public Transfer execute(FileContent fileContent, Instant expiresAt) {
        LOG.infof("Uploading fileName=%s size=%d", fileContent.getFileName(), fileContent.getSize());
        String blobName = fileStoragePort.store(fileContent);
        LOG.infof("File stored blobName=%s", blobName);
        Transfer transfer = Transfer.createNew(fileContent.getFileName(), expiresAt,blobName);
        return transferRepositoryPort.save(transfer);

    }
}
