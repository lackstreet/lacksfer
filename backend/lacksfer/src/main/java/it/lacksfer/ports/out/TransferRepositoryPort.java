package it.lacksfer.ports.out;

import it.lacksfer.domain.Transfer;

import java.util.Optional;
import java.util.UUID;

public interface TransferRepositoryPort {
    Transfer save(Transfer transfer);

    Optional<Transfer> findTransferById(UUID id);
    Optional<Transfer> findByDownloadToken(String downloadToken);
}
