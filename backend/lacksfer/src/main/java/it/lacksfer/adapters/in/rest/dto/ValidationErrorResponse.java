package it.lacksfer.adapters.in.rest.dto;

import java.util.List;

public record ValidationErrorResponse(
        String code,
        String message,
        List<String> errors
) {
}
