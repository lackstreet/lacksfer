package it.lacksfer.adapters.in.rest.safety;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ContentDispositionBuilderTest {
    @Test
    void attachmentShouldBuildHeaderForSafeFileName() {
        String safeFileName = "safe_file_name.txt";
        String contentDisposition ="attachment; filename=\"safe_file_name.txt\"";
        String header = ContentDispositionBuilder.attachment(safeFileName);
        assertEquals(contentDisposition, header);
    }

    @Test
    void attachmentShouldSanitizeUnsafeFileName() {
        String unSafeFileName = "../bad name!.txt";
        String contentDisposition ="attachment; filename=\"bad_name_.txt\"";
        String header = ContentDispositionBuilder.attachment(unSafeFileName);
        assertEquals(contentDisposition, header);
    }

    @Test
    void attachmentShouldRejectBlankFileName() {
        String safeFileName = " ";
        assertThrows(IllegalArgumentException.class, () -> ContentDispositionBuilder.attachment(safeFileName));
    }
}
