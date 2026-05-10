package it.lacksfer.domain.transfer;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TransferTest {
    @Test
    void createNewShouldCreateValidTransfer(){
        Instant expiresAt = Instant.now().plus(10, ChronoUnit.DAYS);
        Transfer transfer = Transfer.createNew("test.txt", expiresAt, "blob-123");
        assertEquals("test.txt", transfer.getFileName());
        assertEquals(expiresAt, transfer.getExpiresAt());
        assertEquals("blob-123", transfer.getBlobName());
        assertNotNull(transfer.getId());
        assertNotNull(transfer.getDownloadToken());
        assertFalse(transfer.isExpired());
    }

    @Test
    void createNewShouldRejectExpiredTransfer(){
        Instant expiresAt = Instant.now().minus(10, ChronoUnit.DAYS);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> Transfer.createNew("test.txt", expiresAt, "blob-123"));
        assertEquals("Expires At cannot be before now", exception.getMessage());
    }
    @Test
    void rehydrateShouldCreateValidTransfer(){
        Instant createdAt = Instant.now().minus(10, ChronoUnit.DAYS);
        Instant expiresAt = Instant.now().plus(10, ChronoUnit.DAYS);
        UUID id = UUID.randomUUID();
        String downloadToken = UUID.randomUUID().toString();


        Transfer transfer = Transfer.rehydrate(id,"test.txt", createdAt, expiresAt, downloadToken, "blob-123");
        assertEquals("blob-123", transfer.getBlobName());
        assertEquals(createdAt, transfer.getCreatedAt());
        assertEquals(expiresAt, transfer.getExpiresAt());
        assertEquals("test.txt", transfer.getFileName());
        assertEquals(id,transfer.getId());
        assertEquals(downloadToken,transfer.getDownloadToken());
        assertFalse(transfer.isExpired());
    }

    @Test
    void isExpiredShouldReturnTrueWhenTransferIsExpired(){
        Instant createdAt = Instant.now().minus(10, ChronoUnit.DAYS);
        Instant expiresAt = Instant.now().minus(9, ChronoUnit.DAYS);
        UUID id = UUID.randomUUID();
        String downloadToken = UUID.randomUUID().toString();


        Transfer transfer = Transfer.rehydrate(id,"test.txt", createdAt, expiresAt, downloadToken, "blob-123");
        assertTrue(transfer.isExpired());
    }
}
