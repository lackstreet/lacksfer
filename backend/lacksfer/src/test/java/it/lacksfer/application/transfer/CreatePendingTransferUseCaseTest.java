package it.lacksfer.application.transfer;

import it.lacksfer.domain.transfer.Transfer;
import it.lacksfer.domain.transfer.TransferStatus;
import it.lacksfer.ports.out.TransferRepositoryPort;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CreatePendingTransferUseCaseTest {

    private static class FakeTransferRepositoryPort implements TransferRepositoryPort {
        private Transfer savedTransfer;

        @Override
        public Transfer save(Transfer transfer) {
            this.savedTransfer = transfer;
            return transfer;
        }

        @Override
        public Optional<Transfer> findTransferById(UUID id) {
            throw new UnsupportedOperationException("Not implemented yet");
        }

        @Override
        public Optional<Transfer> findByDownloadToken(String downloadToken) {
            throw new UnsupportedOperationException("Not implemented yet");
        }
    }

    @Test
    void shouldCreatePendingTransfer() {
        FakeTransferRepositoryPort repository = new FakeTransferRepositoryPort();
        CreatePendingTransferUseCase useCase = new CreatePendingTransferUseCase(repository);
        Instant expiresAt = Instant.now().plus(10, ChronoUnit.DAYS);

        Transfer result = useCase.execute("test.txt", expiresAt, "blob-123", 1024L);

        assertSame(result, repository.savedTransfer);
        assertEquals("test.txt", result.getFileName());
        assertEquals(expiresAt, result.getExpiresAt());
        assertEquals("blob-123", result.getBlobName());
        assertEquals(TransferStatus.PENDING_UPLOAD, result.getStatus());
        assertFalse(result.isReady());
    }
}
