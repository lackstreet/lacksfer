package it.lacksfer.ports.out;

public record StorageFileMetadata(
        boolean exists,
        long sizeBytes
) {
}
