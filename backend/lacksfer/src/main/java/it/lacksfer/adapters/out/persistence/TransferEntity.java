package it.lacksfer.adapters.out.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Table(name="transfers")
public class TransferEntity {
    @Id
    private UUID id;
    @NotNull
    private String fileName;
    @NotNull
    private Instant createdAt;
    @NotNull
    private Instant expiresAt;
    @NotNull
    private String downloadToken;
    @NotNull
    private String blobName;

    protected TransferEntity() {}

    public TransferEntity(UUID id, String fileName, Instant createdAt, Instant expiresAt, String downloadToken, String blobName) {
        this.id = id;
        this.fileName = fileName;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.downloadToken = downloadToken;
        this.blobName = blobName;
    }
}
