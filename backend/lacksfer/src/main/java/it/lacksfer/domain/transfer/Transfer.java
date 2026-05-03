package it.lacksfer.domain.transfer;

import it.lacksfer.domain.common.Require;
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
    private String blobName;

    private Transfer(){}

    public static Transfer createNew(String fileName,Instant expiresAt, String blobName){
        Require.notBlank(fileName,"fileName");
        Require.notNull(expiresAt,"expiresAt");
        Require.notBlank(blobName,"blobName");

        if(expiresAt.isBefore(Instant.now())) {
            throw new IllegalArgumentException("Expires At cannot be before now");
        }
        Transfer transfer = new Transfer();
        transfer.id = UUID.randomUUID();
        transfer.fileName = fileName;
        transfer.createdAt = Instant.now();
        transfer.expiresAt = expiresAt;
        transfer.downloadToken = UUID.randomUUID().toString();
        transfer.blobName = blobName;
        return transfer;
    }

    public static Transfer rehydrate(UUID id, String fileName, Instant createdAt, Instant expiresAt, String downloadToken, String blobName){
        Require.notNull(id,"id");
        Require.notBlank(fileName,"fileName");
        Require.notNull(createdAt,"createdAt");
        Require.notNull(expiresAt,"expiresAt");
        Require.notBlank(downloadToken,"downloadToken");
        Require.notBlank(blobName,"blobName");


        Transfer transfer = new Transfer();
        transfer.id = id;
        transfer.fileName = fileName;
        transfer.createdAt = createdAt;
        transfer.expiresAt = expiresAt;
        transfer.downloadToken = downloadToken;
        transfer.blobName = blobName;
        return transfer;
    }

    public boolean isExpired(){
        return this.expiresAt.isBefore(Instant.now());
    }



}
