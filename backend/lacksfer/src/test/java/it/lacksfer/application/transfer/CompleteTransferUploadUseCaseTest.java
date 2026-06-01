package it.lacksfer.application.transfer;

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

class CompleteTransferUploadUseCaseTest {

    private static class FakeFileStoragePort implements FileStoragePort {

        private final StorageFileMetadata metadata;

        private FakeFileStoragePort(StorageFileMetadata  metadata) {
            this.metadata = metadata;
        }

        @Override
        public String store(FileContent fileContent) {
            return "";
        }

        @Override
        public InputStream download(String blobName) {
            return null;
        }

        @Override
        public UploadUrl createUploadUrl(String blobName) {
            return null;
        }

        @Override
        public StorageFileMetadata getMetadata(String blobName) {
            return metadata;
        }

    }

    private static class FakeTransferRepositoryPort implements TransferRepositoryPort {
        private final Optional<Transfer> transfer;
        private Transfer savedTransfer;


        private FakeTransferRepositoryPort(Optional<Transfer> transfer) {
            this.transfer = transfer;
        }

        @Override
        public Transfer save(Transfer transfer) {
            this.savedTransfer = transfer;
            return transfer;
        }

        @Override
        public Optional<Transfer> findTransferById(UUID id) {
            return transfer;
        }

        @Override
        public Optional<Transfer> findByDownloadToken(String downloadToken) {
            throw new UnsupportedOperationException("Not needed in this test");
        }
    }


    @Test
    void executeShouldMarkTransferAsReady() {
        Transfer transfer = Transfer.createPending("test.txt", Instant.now().plus(10, ChronoUnit.DAYS), "blob-123", 1024L);
        FakeTransferRepositoryPort repository = new FakeTransferRepositoryPort(Optional.of(transfer));
        FakeFileStoragePort fileStoragePort =
                new FakeFileStoragePort(new StorageFileMetadata(true, 1024L));
        CompleteTransferUploadUseCase useCase = new CompleteTransferUploadUseCase(repository, fileStoragePort);

        Transfer result = useCase.execute(transfer.getId());

        assertSame(result, repository.savedTransfer);
        assertEquals(TransferStatus.READY, result.getStatus());
        assertTrue(result.isReady());
    }


    @Test
    void executeShouldRejectMissingTransfer() {
        FakeTransferRepositoryPort repository = new FakeTransferRepositoryPort(Optional.empty());
        FakeFileStoragePort fileStoragePort =
                new FakeFileStoragePort(new StorageFileMetadata(true, 1024L));

        CompleteTransferUploadUseCase useCase = new CompleteTransferUploadUseCase(repository, fileStoragePort);

        assertThrows(TransferNotFoundException.class, () -> useCase.execute(UUID.randomUUID()));
    }

    @Test
    void executeShouldRejectMissingBlob() {
        Transfer transfer = Transfer.createPending("test.txt", Instant.now().plus(10, ChronoUnit.DAYS), "blob-123", 1024L);
        FakeTransferRepositoryPort repository = new FakeTransferRepositoryPort(Optional.of(transfer));
        FakeFileStoragePort fileStoragePort =
                new FakeFileStoragePort(new StorageFileMetadata(false, 0));

        CompleteTransferUploadUseCase useCase = new CompleteTransferUploadUseCase(repository, fileStoragePort);

        assertThrows(IllegalStateException.class, () -> useCase.execute(transfer.getId()));
    }

    @Test
    void executeShouldRejectBlobWithUnexpectedSize() {
        Transfer transfer = Transfer.createPending(
                "test.txt",
                Instant.now().plus(10, ChronoUnit.DAYS),
                "blob-123",
                1024L
        );
        FakeTransferRepositoryPort repository = new FakeTransferRepositoryPort(Optional.of(transfer));
        FakeFileStoragePort fileStoragePort =
                new FakeFileStoragePort(new StorageFileMetadata(true, 512L));

        CompleteTransferUploadUseCase useCase = new CompleteTransferUploadUseCase(repository, fileStoragePort);

        assertThrows(IllegalStateException.class, () -> useCase.execute(transfer.getId()));
    }

}
