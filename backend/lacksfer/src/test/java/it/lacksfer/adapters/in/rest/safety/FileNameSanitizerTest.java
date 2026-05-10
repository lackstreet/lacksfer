package it.lacksfer.adapters.in.rest.safety;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileNameSanitizerTest {

    @Test
    void sanitizeShouldKeepSafeFileName(){
         String fileName = "test.txt";
         String sanitizedFileName = FileNameSanitizer.sanitize(fileName);
         assertEquals(fileName, sanitizedFileName);
     }

    @Test
    void sanitizeShouldRemovePathTraversal(){
        String fileName = "../secret.txt";
        String sanitizedFileName = FileNameSanitizer.sanitize(fileName);
        assertEquals("secret.txt", sanitizedFileName);
    }

    @Test
    void sanitizeShouldHandleWindowsPath(){
         String fileName = "a\\b\\c.txt";
         String sanitizedFileName = FileNameSanitizer.sanitize(fileName);
         assertEquals("c.txt", sanitizedFileName);
    }

    @Test
    void sanitizeShouldReplaceUnsafeCharacters(){
         String fileName = "my file!.txt";
         String sanitizedFileName = FileNameSanitizer.sanitize(fileName);
         assertEquals("my_file_.txt", sanitizedFileName);
    }
    @Test
    void sanitizeShouldRejectBlankFileName(){
         String fileName = "   ";
         assertThrows(IllegalArgumentException.class, () -> FileNameSanitizer.sanitize(fileName));
    }

    @Test
    void sanitizeShouldRejectNullFileName(){
        assertThrows(IllegalArgumentException.class, () -> FileNameSanitizer.sanitize(null));
    }

}
