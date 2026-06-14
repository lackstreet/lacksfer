package it.lacksfer.ports.out;

import it.lacksfer.domain.file.FileContent;

import java.io.InputStream;
import java.util.List;

public interface FileStoragePort {
    String store(FileContent fileContent);
    InputStream download(String blobName);
    UploadUrl createUploadUrl(String blobName);
    StorageFileMetadata getMetadata(String blobName);
    List<String> listUncommittedBlockIds(String blobName);


}
