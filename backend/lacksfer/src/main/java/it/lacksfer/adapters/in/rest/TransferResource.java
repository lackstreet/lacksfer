package it.lacksfer.adapters.in.rest;

import it.lacksfer.adapters.in.rest.dto.request.StartDirectUploadRequest;
import it.lacksfer.adapters.in.rest.dto.response.CompleteTransferUploadResponse;
import it.lacksfer.adapters.in.rest.dto.response.StartDirectUploadResponse;
import it.lacksfer.adapters.in.rest.safety.ContentDispositionBuilder;
import it.lacksfer.adapters.in.rest.safety.FileNameSanitizer;
import it.lacksfer.application.transfer.CompleteTransferUploadUseCase;
import it.lacksfer.application.transfer.StartDirectUploadUseCase;
import it.lacksfer.application.transfer.result.DownloadTransferResult;
import it.lacksfer.adapters.in.rest.dto.response.UploadTransferResponse;
import it.lacksfer.application.transfer.DownloadTransferUseCase;
import it.lacksfer.application.transfer.UploadTransferUseCase;

import it.lacksfer.application.transfer.result.StartDirectUploadResult;
import it.lacksfer.domain.file.FileContent;
import it.lacksfer.domain.transfer.Transfer;
import it.lacksfer.domain.transfer.TransferStatus;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.UUID;

@Path("/transfers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TransferResource {
    private final UploadTransferUseCase uploadTransferUseCase;
    private final DownloadTransferUseCase downloadTransferUseCase;
    private final StartDirectUploadUseCase startDirectUploadUseCase;
    private final CompleteTransferUploadUseCase completeTransferUploadUseCase;

    @ConfigProperty(name = "lacksfer.upload.max-size-bytes")
    long maxUploadSizeBytes;

    public TransferResource(
            UploadTransferUseCase uploadTransferUseCase, DownloadTransferUseCase downloadTransferUseCase, StartDirectUploadUseCase startDirectUploadUseCase, CompleteTransferUploadUseCase completeTransferUploadUseCase) {
        this.uploadTransferUseCase = uploadTransferUseCase;
        this.downloadTransferUseCase = downloadTransferUseCase;
        this.startDirectUploadUseCase = startDirectUploadUseCase;
        this.completeTransferUploadUseCase = completeTransferUploadUseCase;
    }

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response upload(@RestForm("file") FileUpload file, @RestForm("expiresAt") Instant expiresAt) {
        if (file == null) {
            throw new IllegalArgumentException("file is required");
        }
        if (file.size() > maxUploadSizeBytes) {
            throw new IllegalArgumentException("file exceeds maximum allowed size");
        }
        String safeFileName = FileNameSanitizer.sanitize(file.fileName());
        try {
            FileContent fileContent = new FileContent(
                    safeFileName,
                    file.contentType(),
                    file.size(),
                    Files.newInputStream(file.uploadedFile())
            );

            Transfer transfer = uploadTransferUseCase.execute(
                    fileContent, expiresAt
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
                .header("Content-Disposition", ContentDispositionBuilder.attachment(result.fileName()))
                .build();
    }

    @POST
    public Response startDirectUpload(StartDirectUploadRequest request){
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }

        String sanitizedFileName = FileNameSanitizer.sanitize(request.fileName());
        StartDirectUploadResult result = startDirectUploadUseCase.execute(
                sanitizedFileName,
                request.expiresAt()
        );

        Transfer transfer = result.transfer();

        return Response.ok(new StartDirectUploadResponse(
                transfer.getId(),
                transfer.getFileName(),
                transfer.getDownloadToken(),
                result.uploadUrl()
        )).build();

    }

    @POST
    @Path("/{transferId}/complete")
    public Response complete(@PathParam("transferId") UUID transferId){
        if (transferId == null) {
           throw new IllegalArgumentException("transferId is required");
        }
        Transfer transfer = completeTransferUploadUseCase.execute(transferId);
        return Response.ok( new CompleteTransferUploadResponse(
                transfer.getId(),
                transfer.getFileName(),
                transfer.getDownloadToken(),
                transfer.getStatus()
        )).build();

    }



}
