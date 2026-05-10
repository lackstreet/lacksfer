package it.lacksfer.adapters.in.rest.exception.mapper;

import it.lacksfer.adapters.in.rest.dto.ErrorResponse;
import it.lacksfer.domain.exception.TransferExpiredException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class TransferExpiredExceptionMapper implements ExceptionMapper<TransferExpiredException> {
    @Override
    public Response toResponse(TransferExpiredException exception) {
        return Response.status(Response.Status.GONE)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new ErrorResponse(exception.getMessage()))
                .build();
    }
}
