package it.lacksfer.application.transfer;

import it.lacksfer.domain.file.FileContent;
import it.lacksfer.domain.transfer.Transfer;
import it.lacksfer.ports.out.FileStoragePort;
import it.lacksfer.ports.out.TransferRepositoryPort;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.*;

class UploadTransferUseCaseTest {
    private static class FakeFileStoragePort implements FileStoragePort {
        private FileContent storedFileContent;
        @Override
        public String store(FileContent fileContent) {
            this.storedFileContent = fileContent;
            return "blob-123";
        }

        @Override
        public InputStream download(String blobName) {
             throw new UnsupportedOperationException("Not implemented yet");
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
            throw new UnsupportedOperationException("Not implemented yet");
        }

        @Override
        public Optional<Transfer> findByDownloadToken(String downloadToken) {
            throw new UnsupportedOperationException("Not implemented yet");
        }
    }
    @Test
    void executeShouldStoreFileAndSaveTransfer(){
        FakeFileStoragePort fileStoragePort = new FakeFileStoragePort();
        FakeTransferRepositoryPort repositoryPort = new FakeTransferRepositoryPort();
        UploadTransferUseCase useCase = new UploadTransferUseCase(fileStoragePort, repositoryPort);
        Instant expiresAt = Instant.now().plus(10, ChronoUnit.DAYS);
        FileContent fileContent = new FileContent("test.txt", "text/plain", 3, new ByteArrayInputStream(new byte[] {1, 2, 3}));
        Transfer result = useCase.execute(fileContent, expiresAt);

        assertSame(fileContent, fileStoragePort.storedFileContent);
        assertSame(result, repositoryPort.savedTransfer);
        assertEquals("test.txt", result.getFileName());
        assertEquals(expiresAt, result.getExpiresAt());
        assertEquals("blob-123", result.getBlobName());
        assertNotNull(result.getDownloadToken());
    }

}
