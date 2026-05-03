package it.lacksfer.ports.out;

import it.lacksfer.domain.file.FileContent;

public interface FileStoragePort {
    String store(FileContent filecontent);
}
