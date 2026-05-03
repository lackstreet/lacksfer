package it.lacksfer.domain.file;

import it.lacksfer.domain.common.Require;
import lombok.Getter;

import java.io.InputStream;
@Getter
public class FileContent {
    private final String fileName;
    private final String contentType;
    private final long size;
    private final InputStream content;

    public FileContent(String fileName, String contentType, long size, InputStream content){
        Require.notBlank(fileName, "fileName");
        Require.notBlank(contentType, "contentType");
        Require.positive(size, "size");
        Require.notNull(content, "content");

        this.fileName = fileName;
        this.contentType = contentType;
        this.size = size;
        this.content = content;

    }
}
