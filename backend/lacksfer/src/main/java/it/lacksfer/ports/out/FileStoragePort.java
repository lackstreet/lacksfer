package it.lacksfer.ports.out;

import it.lacksfer.domain.file.FileContent;

import java.io.InputStream;

public interface FileStoragePort {
    String store(FileContent fileContent);
    InputStream download(String blobName);
    UploadUrl createUploadUrl(String blobName);
    StorageFileMetadata getMetadata(String blobName);


}
