package it.lacksfer.application.transfer.block;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BlockIdCodecTest {
    private final BlockIdCodec codec = new BlockIdCodec();

    @Test
    void encodeShouldCreateStableBase64BlockId() {
        assertEquals("MDAwMDAw", codec.encode(0));
        assertEquals("MDAwMDAx", codec.encode(1));
        assertEquals("MDAwMDEw", codec.encode(10));
    }

    @Test
    void decodeShouldReadStableBase64BlockId() {
        assertEquals(0, codec.decode("MDAwMDAw"));
        assertEquals(1, codec.decode("MDAwMDAx"));
        assertEquals(10, codec.decode("MDAwMDEw"));
    }

    @Test
    void encodeShouldRejectNegativeIndex() {
        assertThrows(IllegalArgumentException.class, () -> codec.encode(-1));
    }

    @Test
    void decodeShouldRejectBlankBlockId() {
        assertThrows(IllegalArgumentException.class, () -> codec.decode(""));
    }
}