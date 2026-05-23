package it.lacksfer.application.transfer;

import it.lacksfer.application.transfer.result.StartDirectUploadResult;
import it.lacksfer.domain.file.FileContent;
import it.lacksfer.domain.transfer.Transfer;
import it.lacksfer.domain.transfer.TransferStatus;
import it.lacksfer.ports.out.FileStoragePort;
import it.lacksfer.ports.out.TransferRepositoryPort;
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
        public String createUploadUrl(String blobName) {
            this.requestedBlobName = blobName;
            return "https://upload-url.test/" + blobName;
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

    @Test
    void createUploadUrlShouldReturnValidUrl() {
        FakeTransferRepositoryPort repository = new FakeTransferRepositoryPort();
        CreatePendingTransferUseCase createPending = new CreatePendingTransferUseCase(repository);
        FakeFileStoragePort storage = new FakeFileStoragePort();
        StartDirectUploadUseCase useCase = new StartDirectUploadUseCase(createPending, storage);

        Instant expiresAt = Instant.now().plus(10, ChronoUnit.DAYS);

        StartDirectUploadResult result = useCase.execute("test.txt", expiresAt, "blob-123");

        assertSame(result.transfer(), repository.savedTransfer);
        assertEquals(TransferStatus.PENDING_UPLOAD, result.transfer().getStatus());
        assertEquals("blob-123", storage.requestedBlobName);
        assertEquals("https://upload-url.test/blob-123", result.uploadUrl());

    }
}
