package it.lacksfer.adapters.out.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import it.lacksfer.domain.Transfer;
import it.lacksfer.ports.out.TransferRepositoryPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class JpaTransferRepositoryAdapter implements TransferRepositoryPort , PanacheRepositoryBase<TransferEntity, UUID> {
    private final TransferMapper mapper = new TransferMapper();

    @Override
    @Transactional
    public Transfer save(Transfer transfer){
        TransferEntity entity = mapper.toEntity(transfer);
        persist(entity);
        return mapper.toDomain(entity);
    }

    @Override
    public  Optional<Transfer> findTransferById(UUID id){
        return findByIdOptional(id)
                .map(mapper::toDomain);

    }
    @Override
    public Optional<Transfer> findByDownloadToken(String downloadToken){
        return find("downloadToken",downloadToken)
                .firstResultOptional()
                .map(mapper::toDomain);
    }
}
