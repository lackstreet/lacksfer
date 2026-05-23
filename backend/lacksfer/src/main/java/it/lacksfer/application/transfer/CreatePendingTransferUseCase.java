package it.lacksfer.application.transfer;

import it.lacksfer.domain.transfer.Transfer;
import it.lacksfer.ports.out.TransferRepositoryPort;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.time.Instant;

@ApplicationScoped
public class CreatePendingTransferUseCase {
    private final TransferRepositoryPort transferRepositoryPort;
    private static final Logger LOG = Logger.getLogger(CreatePendingTransferUseCase.class);

    public CreatePendingTransferUseCase(TransferRepositoryPort transferRepositoryPort) {
        this.transferRepositoryPort = transferRepositoryPort;
    }

    public Transfer execute(String fileName, Instant expiresAt, String blobName) {
        LOG.infof("Creating pending transfer for fileName=%s", fileName);

        Transfer transfer = Transfer.createPending(fileName, expiresAt, blobName);
        LOG.infof("Saving pending transfer for fileName=%s", fileName);

        return transferRepositoryPort.save(transfer);
    }
}
