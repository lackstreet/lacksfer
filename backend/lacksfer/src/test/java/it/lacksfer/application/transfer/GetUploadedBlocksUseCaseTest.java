package it.lacksfer.application.transfer;

import it.lacksfer.application.transfer.block.BlockIdCodec;
import it.lacksfer.application.transfer.result.GetUploadedBlocksResult;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GetUploadedBlocksUseCaseTest {

    private static final class FakeTransferRepositoryPort implements TransferRepositoryPort {
        private final Optional<Transfer> transfer;

        private FakeTransferRepositoryPort(Optional<Transfer> transfer) {
            this.transfer = transfer;
        }

        @Override
        public Transfer save(Transfer transfer) {
            throw new UnsupportedOperationException("Not needed in this test");
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

    private static final class FakeFileStoragePort implements FileStoragePort {
        private final List<String> blockIds;

        private FakeFileStoragePort(List<String> blockIds) {
            this.blockIds = blockIds;
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
            return null;
        }

        @Override
        public List<String> listUncommittedBlockIds(String blobName) {
            return blockIds;
        }
    }

    @Test
    void executeShouldReturnUploadedBlockIndexesSorted() {
        Transfer transfer = Transfer.createPending(
                "test.txt",
                Instant.now().plus(10, ChronoUnit.DAYS),
                "blob-123",
                1024L
        );

        GetUploadedBlocksUseCase useCase = new GetUploadedBlocksUseCase(
                new FakeTransferRepositoryPort(Optional.of(transfer)),
                new FakeFileStoragePort(List.of("MDAwMDAy", "MDAwMDAw", "MDAwMDAx")),
                new BlockIdCodec()
        );

        GetUploadedBlocksResult result = useCase.execute(transfer.getId());

        assertEquals(transfer.getId(), result.transferId());
        assertEquals(List.of(0, 1, 2), result.uploadedBlockIndexes());

    }

    @Test
    void executeShouldRejectMissingTransfer() {
        GetUploadedBlocksUseCase useCase = new GetUploadedBlocksUseCase(
                new FakeTransferRepositoryPort(Optional.empty()),
                new FakeFileStoragePort(List.of()),
                new BlockIdCodec()
        );

        assertThrows(TransferNotFoundException.class, () -> useCase.execute(UUID.randomUUID()));
    }

    @Test
    void executeShouldRejectExpiredTransfer() {
        Transfer transfer = Transfer.rehydrate(
                UUID.randomUUID(),
                "test.txt",
                Instant.now().minus(10, ChronoUnit.DAYS),
                Instant.now().minus(1, ChronoUnit.DAYS),
                UUID.randomUUID().toString(),
                "blob-123",
                TransferStatus.PENDING_UPLOAD,
                1024L
        );

        GetUploadedBlocksUseCase useCase = new GetUploadedBlocksUseCase(
                new FakeTransferRepositoryPort(Optional.of(transfer)),
                new FakeFileStoragePort(List.of()),
                new BlockIdCodec()
        );

        assertThrows(TransferExpiredException.class, () -> useCase.execute(transfer.getId()));
    }

    @Test
    void executeShouldRejectReadyTransfer() {
        Transfer transfer = Transfer.rehydrate(
                UUID.randomUUID(),
                "test.txt",
                Instant.now().minus(1, ChronoUnit.DAYS),
                Instant.now().plus(10, ChronoUnit.DAYS),
                UUID.randomUUID().toString(),
                "blob-123",
                TransferStatus.READY,
                1024L
        );

        GetUploadedBlocksUseCase useCase = new GetUploadedBlocksUseCase(
                new FakeTransferRepositoryPort(Optional.of(transfer)),
                new FakeFileStoragePort(List.of()),
                new BlockIdCodec()
        );

        assertThrows(IllegalStateException.class, () -> useCase.execute(transfer.getId()));
    }
}
