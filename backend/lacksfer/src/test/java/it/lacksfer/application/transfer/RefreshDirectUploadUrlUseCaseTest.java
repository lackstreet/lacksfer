package it.lacksfer.application.transfer;

import it.lacksfer.application.transfer.result.StartDirectUploadResult;
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

import java.io.InputStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RefreshDirectUploadUrlUseCaseTest {

   private static class FakeTransferRepositoryPort implements TransferRepositoryPort {

       private final Optional<Transfer> transfer;

       private FakeTransferRepositoryPort(Optional<Transfer> transfer) {
           this.transfer = transfer;
       }

       @Override
       public Transfer save(Transfer transfer) {
           throw new UnsupportedOperationException("Not implemented");
       }

       @Override
       public Optional<Transfer> findTransferById(UUID id) {
           return transfer;
       }

       @Override
       public Optional<Transfer> findByDownloadToken(String downloadToken) {
           throw new UnsupportedOperationException("Not implemented");
       }
   }

   private static class FakeFileStoragePort implements FileStoragePort {

       private final UploadUrl uploadUrl;

       private FakeFileStoragePort(UploadUrl uploadUrl) {
           this.uploadUrl = uploadUrl;
       }

       @Override
       public String store(FileContent fileContent) {
           throw new UnsupportedOperationException("Not implemented");
       }

       @Override
       public InputStream download(String blobName) {
           throw new UnsupportedOperationException("Not implemented");
       }

       @Override
       public UploadUrl createUploadUrl(String blobName) {
           return uploadUrl;
       }

       @Override
       public StorageFileMetadata getMetadata(String blobName) {
           throw new UnsupportedOperationException("Not implemented");
       }
   }

    @Test
    void executeShouldRefreshUploadUrlForPendingTransfer() {

        Transfer transfer = Transfer.createPending(
                "test.txt",
                Instant.now().plus(10, ChronoUnit.DAYS),
                "blob-123",
                1024L
        );

        UploadUrl uploadUrl = new UploadUrl(
                "https://upload-url.test/blob-123",
                Instant.now().plus(30, ChronoUnit.MINUTES)
        );

        RefreshDirectUploadUrlUseCase useCase = new RefreshDirectUploadUrlUseCase(
                new FakeTransferRepositoryPort(Optional.of(transfer)),
                new FakeFileStoragePort(uploadUrl)
        );

        StartDirectUploadResult result = useCase.execute(transfer.getId());

        assertSame(transfer, result.transfer());
        assertEquals("https://upload-url.test/blob-123", result.uploadUrl());
    }

    @Test
    void executeShouldRejectMissingTransfer() {
        RefreshDirectUploadUrlUseCase useCase = new RefreshDirectUploadUrlUseCase(
                new FakeTransferRepositoryPort(Optional.empty()),
                new FakeFileStoragePort(new UploadUrl(
                        "https://upload-url.test/blob-123",
                        Instant.now().plus(30, ChronoUnit.MINUTES)
                ))
        );

        assertThrows(TransferNotFoundException.class, () -> useCase.execute(UUID.randomUUID()));
    }

    @Test
    void executeShouldRejectExpiredTransfer() {
        Instant createdAt = Instant.now().minus(10, ChronoUnit.DAYS);
        Instant expiresAt = Instant.now().minus(1, ChronoUnit.DAYS);        UUID id = UUID.randomUUID();
        String downloadToken = UUID.randomUUID().toString();

        Transfer transfer = Transfer.rehydrate(id,"test.txt", createdAt, expiresAt, downloadToken, "blob-123", TransferStatus.PENDING_UPLOAD,1024L);

        RefreshDirectUploadUrlUseCase useCase = new RefreshDirectUploadUrlUseCase(
                new FakeTransferRepositoryPort(Optional.of(transfer)),
                new FakeFileStoragePort(new UploadUrl(
                        "https://upload-url.test/blob-123",
                        Instant.now().plus(30, ChronoUnit.MINUTES)
                ))
        );

        assertThrows(TransferExpiredException.class, () -> useCase.execute(transfer.getId()));
    }

    @Test
    void executeShouldRejectReadyTransfer() {
        Instant createdAt = Instant.now().minus(1, ChronoUnit.DAYS);
        Instant expiresAt = Instant.now().plus(10, ChronoUnit.DAYS);
        UUID id = UUID.randomUUID();
        String downloadToken = UUID.randomUUID().toString();

        Transfer transfer = Transfer.rehydrate(
                id,
                "test.txt",
                createdAt,
                expiresAt,
                downloadToken,
                "blob-123",
                TransferStatus.READY,
                1024L
        );

        RefreshDirectUploadUrlUseCase useCase = new RefreshDirectUploadUrlUseCase(
                new FakeTransferRepositoryPort(Optional.of(transfer)),
                new FakeFileStoragePort(new UploadUrl(
                        "https://upload-url.test/blob-123",
                        Instant.now().plus(30, ChronoUnit.MINUTES)
                ))
        );

        assertThrows(IllegalStateException.class, () -> useCase.execute(transfer.getId()));
    }

}
