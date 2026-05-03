package it.lacksfer.domain;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class Transfer {
    private UUID id;
    private String fileName;
    private Instant createdAt;
    private Instant expiresAt;
    private String downloadToken;

    private static void validateId(UUID id) {
        if(id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
    }
    private static void validateFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }
    }
    private static void validateCreatedAt(Instant createdAt) {
        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt cannot be null");
        }
    }
    private static void validateExpiresAt(Instant expiresAt) {
        if (expiresAt == null) {
            throw new IllegalArgumentException("expiresAt cannot be null");
        }
    }
    private static void validateDownloadToken(String downloadToken) {
        if(downloadToken == null || downloadToken.isBlank()) {
            throw new IllegalArgumentException("Download token cannot be null or empty");
        }
    }





    private Transfer(){}

    public static Transfer createNew(String fileName,Instant expiresAt){
        validateFileName(fileName);
        validateExpiresAt(expiresAt);
        if(expiresAt.isBefore(Instant.now())) {
            throw new IllegalArgumentException("Expires At cannot be before now");
        }
        Transfer transfer = new Transfer();
        transfer.id = UUID.randomUUID();
        transfer.fileName = fileName;
        transfer.createdAt = Instant.now();
        transfer.expiresAt = expiresAt;
        transfer.downloadToken = UUID.randomUUID().toString();
        return transfer;
    }

    public static Transfer rehydrate(UUID id, String fileName, Instant createdAt, Instant expiresAt, String downloadToken){
        validateId(id);
        validateFileName(fileName);
        validateCreatedAt(createdAt);
        validateExpiresAt(expiresAt);
        validateDownloadToken(downloadToken);

        Transfer transfer = new Transfer();
        transfer.id = id;
        transfer.fileName = fileName;
        transfer.createdAt = createdAt;
        transfer.expiresAt = expiresAt;
        transfer.downloadToken = downloadToken;
        return transfer;
    }

    public boolean isExpired(){
        return this.expiresAt.isBefore(Instant.now());
    }



}
