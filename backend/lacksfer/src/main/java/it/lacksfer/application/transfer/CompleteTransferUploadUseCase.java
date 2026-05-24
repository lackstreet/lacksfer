package it.lacksfer.application.transfer;

import it.lacksfer.domain.exception.TransferNotFoundException;
import it.lacksfer.domain.transfer.Transfer;
import it.lacksfer.ports.out.TransferRepositoryPort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class CompleteTransferUploadUseCase {
    private final TransferRepositoryPort transferRepositoryPort;

    public CompleteTransferUploadUseCase(TransferRepositoryPort transferRepositoryPort) {
        this.transferRepositoryPort = transferRepositoryPort;
    }

    public Transfer execute(UUID transferId) {
        if (transferId == null) {
            throw new IllegalArgumentException("transferId is required");
        }
        Transfer transfer = transferRepositoryPort.findTransferById(transferId).orElseThrow(
                () -> new TransferNotFoundException("Transfer not found")
        );

        transfer.markAsReady();
        return transferRepositoryPort.save(transfer);
    }
}
