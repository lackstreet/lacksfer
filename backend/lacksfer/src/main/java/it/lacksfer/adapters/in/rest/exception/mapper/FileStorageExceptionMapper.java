package it.lacksfer.adapters.in.rest.exception.mapper;

import it.lacksfer.adapters.in.rest.dto.ErrorResponse;
import it.lacksfer.application.exception.FileStorageException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class FileStorageExceptionMapper implements ExceptionMapper<FileStorageException> {
    @Override
    public Response toResponse(FileStorageException exception) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new ErrorResponse("FILE_STORAGE_ERROR", exception.getMessage())).build();
    }
}
