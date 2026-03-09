package com.github.amangusss.aiyl_bank_test_aman_nazarkulov.dto;

import java.time.LocalDateTime;

public record ErrorResponse(
        int status,
        String error,
        String message,
        String path,
        String transactionId,
        LocalDateTime timestamp
) {
    public ErrorResponse(int status, String error, String message, String path, String transactionId) {
        this(status, error, message, path, transactionId, LocalDateTime.now());
    }
}
