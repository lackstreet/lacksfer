package it.lacksfer.application.transfer.result;

import java.io.InputStream;

public record DownloadTransferResult (
        String fileName,
        InputStream content
) {
}

