package it.lacksfer.adapters.out.persistence;

import it.lacksfer.domain.transfer.TransferStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Table(name="transfers")
public class TransferEntity {
    @Id
    @Column(name="id")
    private UUID id;
    @NotNull
    @Column(name="file_name")
    private String fileName;
    @NotNull
    @Column(name="created_at")
    private Instant createdAt;
    @NotNull
    @Column(name="expires_at")
    private Instant expiresAt;
    @NotNull
    @Column(name="download_token")
    private String downloadToken;
    @NotNull
    @Column(name="blob_name")
    private String blobName;
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name="status", nullable = false)
    private TransferStatus status;

    @Positive
    @Column(name = "expected_storage_size_bytes", nullable = false)
    private long expectedStorageSizeBytes;

    protected TransferEntity() {}

    public TransferEntity(UUID id, String fileName, Instant createdAt, Instant expiresAt, String downloadToken, String blobName, TransferStatus status, long expectedStorageSizeBytes) {
        this.id = id;
        this.fileName = fileName;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.downloadToken = downloadToken;
        this.blobName = blobName;
        this.status = status;
        this.expectedStorageSizeBytes = expectedStorageSizeBytes;
    }
}
