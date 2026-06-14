package it.lacksfer.application.transfer;

import it.lacksfer.application.transfer.result.DownloadTransferResult;
import it.lacksfer.domain.exception.TransferExpiredException;
import it.lacksfer.domain.exception.TransferNotFoundException;
import it.lacksfer.domain.file.FileContent;
import it.lacksfer.domain.transfer.Transfer;
import it.lacksfer.domain.transfer.TransferStatus;
import it.lacksfer.ports.out.FileStoragePort;
import it.lacksfer.ports.out.StorageFileMetadata;
import it.lacksfer.ports.out.TransferRepositoryPort;
import it.lacksfer.ports.out.UploadUrl;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DownloadTransferUseCaseTest {

     private static class FakeTransferRepositoryPort implements TransferRepositoryPort {

         private final Optional<Transfer> transfer;

         private FakeTransferRepositoryPort(Optional<Transfer> transfer) {
             this.transfer = transfer;
         }
         @Override
         public Transfer save(Transfer transfer) {
             throw new UnsupportedOperationException("Not implemented yet");
         }

         @Override
         public Optional<Transfer> findTransferById(UUID id) {
             throw new UnsupportedOperationException("Not implemented yet");
         }

         @Override
         public Optional<Transfer> findByDownloadToken(String downloadToken){
             return transfer;
         }
     }

     private static class FakeFileStoragePort implements FileStoragePort {
         private String downloadedBlobName;


         @Override
         public String store(FileContent fileContent) {
             throw new UnsupportedOperationException("Not implemented yet");
         }

         @Override
         public InputStream download(String blobName) {
             this.downloadedBlobName = blobName;
             return new ByteArrayInputStream(new byte[] {1, 2, 3});

         }

         @Override
         public UploadUrl createUploadUrl(String blobName) {
             throw new UnsupportedOperationException("Not implemented yet");
         }

         @Override
         public StorageFileMetadata getMetadata(String blobName) {
             return null;
         }

         @Override
         public List<String> listUncommittedBlockIds(String blobName) {
             return List.of();
         }

     }
     @Test
     void executeShouldDownloadStoredBlobWhenTransferExists(){
         String downloadToken = "token-123";
         Instant expiresAt = Instant.now().plus(10, ChronoUnit.DAYS);
         Instant createdAt = Instant.now().minus(10, ChronoUnit.DAYS);
         Transfer transfer = Transfer.rehydrate(UUID.randomUUID(), "file.txt", createdAt, expiresAt, downloadToken, "blob-123", TransferStatus.READY, 1024L);
         TransferRepositoryPort transferRepositoryPort = new FakeTransferRepositoryPort(Optional.of(transfer));
         FakeFileStoragePort fileStoragePort = new FakeFileStoragePort();
         DownloadTransferUseCase useCase = new DownloadTransferUseCase(transferRepositoryPort, fileStoragePort);
         DownloadTransferResult result = useCase.execute(downloadToken);

         assertEquals("blob-123", fileStoragePort.downloadedBlobName);
         assertEquals("file.txt", result.fileName());
         assertNotNull(result.content());
     }

     @Test
    void executeShouldRejectMissingTransfer() {
        FakeTransferRepositoryPort repositoryPort = new FakeTransferRepositoryPort(Optional.empty());
        FakeFileStoragePort fileStoragePort = new FakeFileStoragePort();
        DownloadTransferUseCase useCase = new DownloadTransferUseCase(repositoryPort, fileStoragePort);

        assertThrows(TransferNotFoundException.class, () -> useCase.execute("missing-token"));
    }


    @Test
    void executeShouldRejectExpiredTransfer(){
        String downloadToken = "token-123";

        Instant expiresAt = Instant.now().minus(10, ChronoUnit.DAYS);
        Instant createdAt = Instant.now().minus(11, ChronoUnit.DAYS);
        Transfer transfer = Transfer.rehydrate(UUID.randomUUID(), "file.txt", createdAt, expiresAt, downloadToken, "blob-123", TransferStatus.READY, 1024L);

        TransferRepositoryPort transferRepositoryPort = new FakeTransferRepositoryPort(Optional.of(transfer));
        FakeFileStoragePort fileStoragePort = new FakeFileStoragePort();
        DownloadTransferUseCase useCase = new DownloadTransferUseCase(transferRepositoryPort, fileStoragePort);

        assertThrows(TransferExpiredException.class, () -> useCase.execute(downloadToken));
        assertNull(fileStoragePort.downloadedBlobName);

    }

    @Test
    void executeShouldRejectBlankToken(){
        FakeTransferRepositoryPort repositoryPort = new FakeTransferRepositoryPort(Optional.empty());
        FakeFileStoragePort fileStoragePort = new FakeFileStoragePort();
        DownloadTransferUseCase useCase = new DownloadTransferUseCase(repositoryPort, fileStoragePort);

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(" "));
        assertNull(fileStoragePort.downloadedBlobName);

    }

    @Test
    void executeShouldRejectPendingTransfer() {
        String downloadToken = "token-123";

        Instant expiresAt = Instant.now().plus(1, ChronoUnit.DAYS);
        Instant createdAt = Instant.now().minus(1, ChronoUnit.DAYS);
        Transfer transfer = Transfer.rehydrate(UUID.randomUUID(), "file.txt", createdAt, expiresAt, downloadToken, "blob-123", TransferStatus.PENDING_UPLOAD, 1024L);

        TransferRepositoryPort transferRepositoryPort = new FakeTransferRepositoryPort(Optional.of(transfer));
        FakeFileStoragePort fileStoragePort = new FakeFileStoragePort();
        DownloadTransferUseCase useCase = new DownloadTransferUseCase(transferRepositoryPort, fileStoragePort);

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(downloadToken));
        assertNull(fileStoragePort.downloadedBlobName);
    }


}
