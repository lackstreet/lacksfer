package it.lacksfer.application.transfer;

import it.lacksfer.domain.Transfer;
import it.lacksfer.ports.out.TransferRepositoryPort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class GetTransferByDownloadTokenUseCase {
    private final TransferRepositoryPort transferRepositoryPort;

    public GetTransferByDownloadTokenUseCase(TransferRepositoryPort transferRepositoryPort) {
        this.transferRepositoryPort = transferRepositoryPort;
    }

    public Optional<Transfer> execute(String downloadToken) {
        if (downloadToken == null || downloadToken.isBlank()) {
            throw new IllegalArgumentException("downloadToken cannot be null or blank");
        }
        return transferRepositoryPort.findByDownloadToken(downloadToken);
    }
}
