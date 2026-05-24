package it.lacksfer.adapters.in.rest.dto.response;

public record ErrorResponse(
        String code,
        String message
) {
}
