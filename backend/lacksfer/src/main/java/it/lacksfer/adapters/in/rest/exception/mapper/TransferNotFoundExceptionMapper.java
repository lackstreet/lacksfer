package it.lacksfer.adapters.in.rest.exception.mapper;

import it.lacksfer.adapters.in.rest.dto.ErrorResponse;
import it.lacksfer.domain.exception.TransferNotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class TransferNotFoundExceptionMapper implements ExceptionMapper<TransferNotFoundException> {
    @Override
    public Response toResponse(TransferNotFoundException exception) {
        return Response.status(Response.Status.NOT_FOUND)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new ErrorResponse(exception.getMessage()))
                .build();
    }
}
