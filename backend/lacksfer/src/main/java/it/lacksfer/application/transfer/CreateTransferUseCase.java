package it.lacksfer.application.transfer;

import it.lacksfer.domain.Transfer;
import it.lacksfer.ports.out.TransferRepositoryPort;
import jakarta.enterprise.context.ApplicationScoped;


import java.time.Instant;

@ApplicationScoped
public class CreateTransferUseCase {

    private final TransferRepositoryPort repository;

    public CreateTransferUseCase(TransferRepositoryPort repository){
        this.repository = repository;
    }

    public Transfer execute(String fileName, Instant expiresAt){
        Transfer transfer = Transfer.createNew(fileName, expiresAt);
        return repository.save(transfer);
    }
}
