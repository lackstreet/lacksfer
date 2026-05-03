package it.lacksfer.application.transfer;

import it.lacksfer.domain.transfer.Transfer;
import it.lacksfer.ports.out.TransferRepositoryPort;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.util.Optional;

@ApplicationScoped
public class GetTransferByDownloadTokenUseCase {
    private final TransferRepositoryPort transferRepositoryPort;
    private static final Logger LOG = Logger.getLogger(GetTransferByDownloadTokenUseCase.class);
    public GetTransferByDownloadTokenUseCase(TransferRepositoryPort transferRepositoryPort) {
        this.transferRepositoryPort = transferRepositoryPort;
    }

    public Optional<Transfer> execute(String downloadToken) {
        LOG.infof("Search for downloadToken=%s", downloadToken);

        if (downloadToken == null || downloadToken.isBlank()) {
            throw new IllegalArgumentException("downloadToken cannot be null or blank");
        }
        return transferRepositoryPort.findByDownloadToken(downloadToken);
    }
}
