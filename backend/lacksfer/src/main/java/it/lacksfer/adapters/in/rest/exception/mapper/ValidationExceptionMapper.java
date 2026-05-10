package it.lacksfer.adapters.in.rest.exception.mapper;

import it.lacksfer.adapters.in.rest.dto.ValidationErrorResponse;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.List;
@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {
    @Override
    public Response toResponse(ConstraintViolationException exception) {
        List<String> errors = exception.getConstraintViolations().stream().map(v -> v.getMessage()).toList();
        return Response.status(Response.Status.BAD_REQUEST).entity(new ValidationErrorResponse("Validation failed", errors))
                .type(MediaType.APPLICATION_JSON_TYPE).build();
    }
}
