package it.lacksfer.adapters.out.storage;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import it.lacksfer.application.exception.FileStorageException;
import it.lacksfer.domain.file.FileContent;
import it.lacksfer.ports.out.FileStoragePort;
import it.lacksfer.ports.out.StorageFileMetadata;
import it.lacksfer.ports.out.UploadUrl;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.UUID;
@ApplicationScoped
public class AzuriteFileStorageAdapter implements FileStoragePort {

    private static final long MAX_TIME_TO_LIVE_MINUTES = 30;
    private static final long MIN_TIME_TO_LIVE_MINUTES = 5;
    private static final String UPLOAD_URL_FORMAT = "%s?%s";

    private final BlobContainerClient containerClient;

    public AzuriteFileStorageAdapter(@ConfigProperty(name = "azurite.connection-string") String connectionString,
                                     @ConfigProperty(name = "azurite.container-name") String containerName) {
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(connectionString).buildClient();
        this.containerClient = blobServiceClient.getBlobContainerClient(containerName);
        if (!containerClient.exists()) {
            containerClient.create();
        }
    }

    @Override
    public String store(FileContent fileContent) {
        String blobName = UUID.randomUUID().toString();
        BlobClient blobClient = containerClient.getBlobClient(blobName);

        try {
            blobClient.upload(
                    fileContent.getContent(),
                    fileContent.getSize(),
                    true
            );
        } catch (Exception e) {
            throw new FileStorageException("File storage operation failed", e);
        }

        return blobName;
    }

    @Override
    public InputStream download(String blobName) {
        try {
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            if (!blobClient.exists()) {
                throw new IllegalArgumentException("File not found");
            }
            return blobClient.openInputStream();
        }catch (Exception e){
            throw new FileStorageException("File storage operation failed", e);
        }
    }

    @Override
    public UploadUrl createUploadUrl(String blobName) {
        try{
            OffsetDateTime expiresAt = OffsetDateTime.now().plusMinutes(MAX_TIME_TO_LIVE_MINUTES);
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            BlobSasPermission permission = new BlobSasPermission()
                    .setCreatePermission(true)
                    .setWritePermission(true);

            BlobServiceSasSignatureValues values = new BlobServiceSasSignatureValues(
                    expiresAt,
                    permission
            ).setStartTime(OffsetDateTime.now().minusMinutes(MIN_TIME_TO_LIVE_MINUTES));

            String sasToken = blobClient.generateSas(values);
            return new UploadUrl(String.format(UPLOAD_URL_FORMAT,blobClient.getBlobUrl(),sasToken),expiresAt.toInstant());
        }catch (Exception e){
            throw new FileStorageException("File storage operation failed", e);
        }
    }

    @Override
    public StorageFileMetadata getMetadata(String blobName) {
        try {
            if (blobName == null || blobName.isBlank()) {
                return new StorageFileMetadata(false, 0);
            }

            BlobClient blobClient = containerClient.getBlobClient(blobName);
            if (!blobClient.exists()) {
                return new StorageFileMetadata(false, 0);
            }
            return new StorageFileMetadata(true, blobClient.getProperties().getBlobSize());
        } catch (Exception e) {
            throw new FileStorageException("File storage operation failed", e);
        }
    }

}
