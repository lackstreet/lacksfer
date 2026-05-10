package it.lacksfer.adapters.in.rest.safety;

public class ContentDispositionBuilder {
    public static String attachment(String fileName) {
        return String.format("attachment; filename=\"%s\"", FileNameSanitizer.sanitize(fileName));
    }
}
