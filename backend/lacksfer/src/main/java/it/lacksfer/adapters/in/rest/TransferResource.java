package it.lacksfer.adapters.in.rest;

import it.lacksfer.adapters.in.rest.dto.CreateTransferRequest;
import it.lacksfer.adapters.in.rest.dto.CreateTransferResponse;
import it.lacksfer.application.transfer.CreateTransferUseCase;
import it.lacksfer.domain.Transfer;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/transfers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TransferResource {
    private final CreateTransferUseCase createTransferUseCase;

    public TransferResource(CreateTransferUseCase createTransferUseCase) {
        this.createTransferUseCase = createTransferUseCase;
    }

    @POST
    public CreateTransferResponse create(CreateTransferRequest request){
        Transfer transfer = createTransferUseCase.execute(
                request.fileName(),
                request.expiresAt()
        );
        return new CreateTransferResponse(
                transfer.getId(),
                transfer.getFileName(),
                transfer.getDownloadToken()
        );
    }

}
