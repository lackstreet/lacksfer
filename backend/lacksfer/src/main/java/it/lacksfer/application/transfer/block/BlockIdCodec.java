package it.lacksfer.application.transfer.block;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class BlockIdCodec {
    private static final int PADDED_INDEX_LENGTH = 6;

    public String encode (int index) {
        if (index < 0) {
            throw new IllegalArgumentException("block index must be greater than or equal to zero");
        }

        String paddedIndex = String.format("%0" + PADDED_INDEX_LENGTH + "d", index);

        return Base64.getEncoder().encodeToString(paddedIndex.getBytes(StandardCharsets.UTF_8));
    }

    public int decode (String blockId) {
        if (blockId == null || (blockId.isBlank())) {
            throw new IllegalArgumentException("block is required");
        }

        String decoded = new String (
                Base64.getDecoder().decode(blockId),
                StandardCharsets.UTF_8
        );

        return Integer.parseInt(decoded);
    }
}
