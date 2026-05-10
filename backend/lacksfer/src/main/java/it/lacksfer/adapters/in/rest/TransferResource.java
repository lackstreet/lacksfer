package it.lacksfer.adapters.in.rest;

import it.lacksfer.adapters.in.rest.safety.FileNameSanitizer;
import it.lacksfer.application.transfer.DownloadTransferResult;
import it.lacksfer.adapters.in.rest.dto.UploadTransferForm;
import it.lacksfer.adapters.in.rest.dto.UploadTransferResponse;
import it.lacksfer.application.transfer.DownloadTransferUseCase;
import it.lacksfer.application.transfer.UploadTransferUseCase;

import it.lacksfer.domain.file.FileContent;
import it.lacksfer.domain.transfer.Transfer;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.MultipartForm;

import java.io.IOException;
import java.nio.file.Files;

@Path("/transfers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TransferResource {
    private final UploadTransferUseCase uploadTransferUseCase;
    private final DownloadTransferUseCase downloadTransferUseCase;
    @ConfigProperty(name = "lacksfer.upload.max-size-bytes")
    long maxUploadSizeBytes;

    public TransferResource(
            UploadTransferUseCase uploadTransferUseCase, DownloadTransferUseCase downloadTransferUseCase) {
        this.uploadTransferUseCase = uploadTransferUseCase;
        this.downloadTransferUseCase = downloadTransferUseCase;
    }

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response upload(@MultipartForm UploadTransferForm form) {
        if (form == null || form.file == null) {
            throw new IllegalArgumentException("file is required");
        }
        if (form.file.size() > maxUploadSizeBytes) {
            throw new IllegalArgumentException("file exceeds maximum allowed size");
        }
        String safeFileName = FileNameSanitizer.sanitize(form.file.fileName());
        try {
            FileContent fileContent = new FileContent(
                    safeFileName,
                    form.file.contentType(),
                    form.file.size(),
                    Files.newInputStream(form.file.uploadedFile())
            );

            Transfer transfer = uploadTransferUseCase.execute(
                    fileContent, form.expiresAt
            );
            return Response.ok(new UploadTransferResponse(
                    transfer.getId(),
                    transfer.getFileName(),
                    transfer.getDownloadToken()
            )).build();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read uploaded file", e);
        }
    }


    @GET
    @Path("/{downloadToken}/download")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response download(@PathParam("downloadToken") String downloadToken) {
        if (downloadToken == null || downloadToken.isBlank()) {
            throw new IllegalArgumentException("downloadToken link required");
        }

        DownloadTransferResult result = downloadTransferUseCase.execute(downloadToken);

        return Response.ok(result.content())
                .header("Content-Disposition", "attachment; filename=\"" + result.fileName() + "\"")
                .build();
    }
}
