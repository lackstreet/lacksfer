package it.lacksfer.adapters.in.rest;

import it.lacksfer.adapters.in.rest.dto.request.StartDirectUploadRequest;
import it.lacksfer.adapters.in.rest.dto.response.CompleteTransferUploadResponse;
import it.lacksfer.adapters.in.rest.dto.response.GetUploadedBlocksResponse;
import it.lacksfer.adapters.in.rest.dto.response.StartDirectUploadResponse;
import it.lacksfer.adapters.in.rest.safety.ContentDispositionBuilder;
import it.lacksfer.adapters.in.rest.safety.FileNameSanitizer;
import it.lacksfer.application.transfer.CompleteTransferUploadUseCase;
import it.lacksfer.application.transfer.DownloadTransferUseCase;
import it.lacksfer.application.transfer.GetUploadedBlocksUseCase;
import it.lacksfer.application.transfer.RefreshDirectUploadUrlUseCase;
import it.lacksfer.application.transfer.StartDirectUploadUseCase;

import it.lacksfer.application.transfer.result.DownloadTransferResult;
import it.lacksfer.application.transfer.result.GetUploadedBlocksResult;
import it.lacksfer.application.transfer.result.StartDirectUploadResult;
import it.lacksfer.domain.transfer.Transfer;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@Path("/transfers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TransferResource {

    private final DownloadTransferUseCase downloadTransferUseCase;
    private final StartDirectUploadUseCase startDirectUploadUseCase;
    private final CompleteTransferUploadUseCase completeTransferUploadUseCase;
    private final RefreshDirectUploadUrlUseCase refreshDirectUploadUrlUseCase;
    private final GetUploadedBlocksUseCase getUploadedBlockUseCase;

    public TransferResource(DownloadTransferUseCase downloadTransferUseCase, StartDirectUploadUseCase startDirectUploadUseCase, CompleteTransferUploadUseCase completeTransferUploadUseCase, RefreshDirectUploadUrlUseCase refreshDirectUploadUrlUseCase, GetUploadedBlocksUseCase getUploadedBlockUseCase) {
        this.downloadTransferUseCase = downloadTransferUseCase;
        this.startDirectUploadUseCase = startDirectUploadUseCase;
        this.completeTransferUploadUseCase = completeTransferUploadUseCase;
        this.refreshDirectUploadUrlUseCase = refreshDirectUploadUrlUseCase;
        this.getUploadedBlockUseCase = getUploadedBlockUseCase;
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
                request.expiresAt(),
                request.expectedStorageSizeBytes()
        );

        Transfer transfer = result.transfer();

        return Response.ok(new StartDirectUploadResponse(
                transfer.getId(),
                transfer.getFileName(),
                transfer.getDownloadToken(),
                result.uploadUrl(),
                result.uploadUrlExpiresAt()
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

    @POST
    @Path("/{transferId}/upload-url")
    public Response refreshUploadUrl(@PathParam("transferId") UUID transferId) {
        if (transferId == null) {
            throw new IllegalArgumentException("transferId is required");
        }

        StartDirectUploadResult result = refreshDirectUploadUrlUseCase.execute(transferId);

        Transfer transfer = result.transfer();

        return Response.ok(new StartDirectUploadResponse(
                transfer.getId(),
                transfer.getFileName(),
                transfer.getDownloadToken(),
                result.uploadUrl(),
                result.uploadUrlExpiresAt()
        )).build();
    }

    @GET
    @Path("/{transferId}/uploaded-blocks")
    public Response getUploadedBlocks(@PathParam("transferId") UUID transferId)  {
        if (transferId == null) {
            throw new IllegalArgumentException("transferId is required");
        }

        GetUploadedBlocksResult result = getUploadedBlockUseCase.execute(transferId);

        return Response.ok(new GetUploadedBlocksResponse(
            result.transferId(),
            result.uploadedBlockIndexes()
        )).build();
    }



}
