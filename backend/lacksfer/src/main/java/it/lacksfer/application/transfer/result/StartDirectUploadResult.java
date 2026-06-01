package it.lacksfer.application.transfer.result;

import it.lacksfer.domain.transfer.Transfer;

public record StartDirectUploadResult(
        Transfer transfer,
        String uploadUrl,
        String uploadUrlExpiresAt
) {}
