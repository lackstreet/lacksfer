package it.lacksfer.application.transfer;

import it.lacksfer.domain.exception.TransferNotFoundException;
import it.lacksfer.domain.transfer.Transfer;
import it.lacksfer.domain.transfer.TransferStatus;
import it.lacksfer.ports.out.TransferRepositoryPort;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CompleteTransferUploadUseCaseTest {

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
        Transfer transfer = Transfer.createPending("test.txt", Instant.now().plus(10, ChronoUnit.DAYS), "blob-123");
        FakeTransferRepositoryPort repository = new FakeTransferRepositoryPort(Optional.of(transfer));
        CompleteTransferUploadUseCase useCase = new CompleteTransferUploadUseCase(repository);

        Transfer result = useCase.execute(transfer.getId());

        assertSame(result, repository.savedTransfer);
        assertEquals(TransferStatus.READY, result.getStatus());
        assertTrue(result.isReady());
    }


    @Test
    void executeShouldRejectMissingTransfer() {
        FakeTransferRepositoryPort repository = new FakeTransferRepositoryPort(Optional.empty());
        CompleteTransferUploadUseCase useCase = new CompleteTransferUploadUseCase(repository);

        assertThrows(TransferNotFoundException.class, () -> useCase.execute(UUID.randomUUID()));
    }

}
