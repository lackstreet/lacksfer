package it.lacksfer.adapters.out.persistence.mapper;

import it.lacksfer.adapters.out.persistence.TransferEntity;
import it.lacksfer.domain.transfer.Transfer;

public class TransferMapper {
    public Transfer toDomain(TransferEntity entity) {
        return Transfer.rehydrate(
                entity.getId(),
                entity.getFileName(),
                entity.getCreatedAt(),
                entity.getExpiresAt(),
                entity.getDownloadToken(),
                entity.getBlobName()
        );
    }

    public TransferEntity toEntity(Transfer transfer){
        return new TransferEntity(
                transfer.getId(),
                transfer.getFileName(),
                transfer.getCreatedAt(),
                transfer.getExpiresAt(),
                transfer.getDownloadToken(),
                transfer.getBlobName()
        );
    }
}
