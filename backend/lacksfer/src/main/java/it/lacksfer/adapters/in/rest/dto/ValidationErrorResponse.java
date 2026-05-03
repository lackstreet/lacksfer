package it.lacksfer.adapters.in.rest.dto;

import java.util.List;

public record ValidationErrorResponse(
        String message,
        List<String> errors
) {
}
