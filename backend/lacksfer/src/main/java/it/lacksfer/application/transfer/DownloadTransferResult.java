package it.lacksfer.application.transfer;

import java.io.InputStream;

public record DownloadTransferResult (
        String fileName,
        InputStream content
) {
}

