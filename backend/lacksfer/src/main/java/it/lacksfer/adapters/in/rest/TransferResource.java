package it.lacksfer.adapters.in.rest;

import it.lacksfer.adapters.in.rest.dto.ErrorResponse;
import it.lacksfer.adapters.in.rest.dto.GetTransferResponse;
import it.lacksfer.application.transfer.GetTransferByDownloadTokenUseCase;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;


@Path("/transfers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TransferResource {
    private final GetTransferByDownloadTokenUseCase getTransferByDownloadTokenUseCase;

    public TransferResource(
            GetTransferByDownloadTokenUseCase getTransferByDownloadTokenUseCase
    ) {
        this.getTransferByDownloadTokenUseCase = getTransferByDownloadTokenUseCase;
    }


    @GET
    @Path("/{downloadToken}")
    public Response getByDownloadToken(@PathParam("downloadToken") String downloadToken) {
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
