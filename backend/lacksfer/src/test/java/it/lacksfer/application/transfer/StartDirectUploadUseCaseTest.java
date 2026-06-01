package it.lacksfer.application.transfer;

import it.lacksfer.application.transfer.result.StartDirectUploadResult;
import it.lacksfer.domain.file.FileContent;
import it.lacksfer.domain.transfer.Transfer;
import it.lacksfer.domain.transfer.TransferStatus;
import it.lacksfer.ports.out.*;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class StartDirectUploadUseCaseTest {

    private static class FakeFileStoragePort implements FileStoragePort {
        private String requestedBlobName;

        @Override
        public String store(FileContent fileContent) {
            throw new UnsupportedOperationException("Not implemented yet");
        }

        @Override
        public InputStream download(String blobName) {
            throw new UnsupportedOperationException("Not implemented yet");
        }

        @Override
        public UploadUrl createUploadUrl(String blobName) {
            this.requestedBlobName = blobName;
            return new UploadUrl("https://upload-url.test/" + blobName, Instant.now().plus(5, ChronoUnit.DAYS));
        }

        @Override
        public StorageFileMetadata getMetadata(String blobName) {
            return null;
        }


    }

    private static class FakeTransferRepositoryPort implements TransferRepositoryPort {
        private Transfer savedTransfer;
        @Override
        public Transfer save(Transfer transfer) {
            this.savedTransfer = transfer;
            return transfer;
        }

        @Override
        public Optional<Transfer> findTransferById(UUID id) {
            throw new UnsupportedOperationException("Not needed in this test");
        }

        @Override
        public Optional<Transfer> findByDownloadToken(String downloadToken) {
            throw new UnsupportedOperationException("Not needed in this test");
        }
    }

    private static class FakeBlobNameGeneratorPort implements BlobNameGeneratorPort {
        @Override
        public String generate() {
            return "blob-123";
        }
    }

    @Test
    void executeShouldCreatePendingTransferAndUploadUrl() {
        FakeTransferRepositoryPort repository = new FakeTransferRepositoryPort();
        CreatePendingTransferUseCase createPending = new CreatePendingTransferUseCase(repository);
        FakeFileStoragePort storage = new FakeFileStoragePort();
        FakeBlobNameGeneratorPort generator = new FakeBlobNameGeneratorPort();
        StartDirectUploadUseCase useCase = new StartDirectUploadUseCase(createPending, storage, generator);

        Instant expiresAt = Instant.now().plus(10, ChronoUnit.DAYS);

        StartDirectUploadResult result = useCase.execute("test.txt", expiresAt, 1024L);

        assertSame(result.transfer(), repository.savedTransfer);
        assertEquals(TransferStatus.PENDING_UPLOAD, result.transfer().getStatus());
        assertEquals("blob-123", storage.requestedBlobName);
        assertEquals("https://upload-url.test/blob-123", result.uploadUrl());

    }
}
