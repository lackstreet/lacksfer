package it.lacksfer.adapters.in.rest.safety;

public class FileNameSanitizer {
    public static String sanitize(String fileName) {
        if (fileName == null) {
            throw new IllegalArgumentException("File name cannot be null");
        }

        String normalized = fileName.trim().replace("\\", "/");
        String baseName = normalized.substring(normalized.lastIndexOf('/') + 1);
        baseName = baseName.replaceAll("[^a-zA-Z0-9_\\-\\.]", "_");

        if (baseName.isBlank()) {
            throw new IllegalArgumentException("File name cannot be blank");
        }
        if (baseName.length() > 255) {
            throw new IllegalArgumentException("File name cannot exceed 255 characters");
        }

        return baseName;
    }
}
