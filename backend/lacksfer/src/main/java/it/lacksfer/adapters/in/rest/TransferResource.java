package it.lacksfer.adapters.in.rest;

import it.lacksfer.adapters.in.rest.dto.ErrorResponse;
import it.lacksfer.adapters.in.rest.dto.GetTransferResponse;
import it.lacksfer.adapters.in.rest.dto.UploadTransferForm;
import it.lacksfer.adapters.in.rest.dto.UploadTransferResponse;
import it.lacksfer.application.transfer.DownloadTransferUseCase;
import it.lacksfer.application.transfer.GetTransferByDownloadTokenUseCase;
import it.lacksfer.application.transfer.UploadTransferUseCase;

import it.lacksfer.domain.file.FileContent;
import it.lacksfer.domain.transfer.Transfer;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.MultipartForm;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

@Path("/transfers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TransferResource {
    private final GetTransferByDownloadTokenUseCase getTransferByDownloadTokenUseCase;
    private final UploadTransferUseCase uploadTransferUseCase;
    private final DownloadTransferUseCase downloadTransferUseCase;

    public TransferResource(
            GetTransferByDownloadTokenUseCase getTransferByDownloadTokenUseCase,
            UploadTransferUseCase uploadTransferUseCase, DownloadTransferUseCase downloadTransferUseCase) {
        this.getTransferByDownloadTokenUseCase = getTransferByDownloadTokenUseCase;
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
        try{
            FileContent fileContent = new FileContent(
                    form.file.fileName(),
                    form.file.contentType(),
                    form.file.size(),
                    Files.newInputStream(form.file.uploadedFile())
            );

            Transfer transfer = uploadTransferUseCase.execute(
                    fileContent,form.expiresAt
            );
            return Response.ok(new UploadTransferResponse(
                    transfer.getId(),
                    transfer.getFileName(),
                    transfer.getDownloadToken()
            )).build();
        } catch (IOException e ){
            throw new RuntimeException("Failed to read uploaded file",e);
        }
    }


    @GET
    @Path("/{downloadToken}/download")
    public Response getByDownloadToken(@PathParam("downloadToken") String downloadToken) {
        if(downloadToken == null || downloadToken.isBlank())
            throw new IllegalArgumentException("downloadToken link required");
        InputStream stream = downloadTransferUseCase.execute(downloadToken);

        return getTransferByDownloadTokenUseCase.execute(downloadToken)
                .map(transfer -> Response.ok(new GetTransferResponse(
                        transfer.getId(),
                        transfer.getFileName(),
                        transfer.isExpired()
                )).build())
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Transfer not found"))
                        .build());
    }

}
