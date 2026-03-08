package com.github.amangusss.aiyl_bank_test_aman_nazarkulov.dto;

import com.github.amangusss.aiyl_bank_test_aman_nazarkulov.entity.transfer.TransferStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class TransferDTO {

    private TransferDTO() {
        throw new UnsupportedOperationException("Cannot create instance of the high-level DTO");
    }

    public static class Request {
        public record Transfer(
            @NotNull Long senderId,
            @NotNull Long receiverId,
            @Positive double amount,
            @NotBlank String currency
        ) {}
    }

    public static class Response {
        public record Transfer(
            Long id,
            Long senderId,
            Long receiverId,
            double amount,
            String currency,
            TransferStatus status
        ) {}
    }
}
