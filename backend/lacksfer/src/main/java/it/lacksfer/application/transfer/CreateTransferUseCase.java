package it.lacksfer.application.transfer;

import it.lacksfer.domain.Transfer;
import it.lacksfer.ports.out.TransferRepositoryPort;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;


import java.time.Instant;

@ApplicationScoped
public class CreateTransferUseCase {

    private final TransferRepositoryPort repository;
    private static final Logger LOG = Logger.getLogger(CreateTransferUseCase.class);

    public CreateTransferUseCase(TransferRepositoryPort repository){
        this.repository = repository;
    }

    public Transfer execute(String fileName, Instant expiresAt){
        LOG.infof("Creating transfer for fileName=%s", fileName);

        Transfer transfer = Transfer.createNew(fileName, expiresAt);
        Transfer saved = repository.save(transfer);

        LOG.infof("Transfer created id=%s",saved.getId());

        return saved;
    }
}
