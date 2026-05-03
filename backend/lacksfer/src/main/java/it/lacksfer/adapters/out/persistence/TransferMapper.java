package it.lacksfer.adapters.out.persistence;

import it.lacksfer.domain.Transfer;

public class TransferMapper {
    public Transfer toDomain(TransferEntity entity) {
        return Transfer.rehydrate(
                entity.getId(),
                entity.getFileName(),
                entity.getCreatedAt(),
                entity.getExpiresAt(),
                entity.getDownloadToken()
        );
    }

    public TransferEntity toEntity(Transfer transfer){
        return new TransferEntity(
                transfer.getId(),
                transfer.getFileName(),
                transfer.getCreatedAt(),
                transfer.getExpiresAt(),
                transfer.getDownloadToken()
        );
    }
}
