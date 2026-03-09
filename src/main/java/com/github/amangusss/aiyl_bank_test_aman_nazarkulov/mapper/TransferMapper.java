package com.github.amangusss.aiyl_bank_test_aman_nazarkulov.mapper;

import com.github.amangusss.aiyl_bank_test_aman_nazarkulov.dto.TransferDTO;
import com.github.amangusss.aiyl_bank_test_aman_nazarkulov.entity.transfer.Transfer;
import org.springframework.stereotype.Component;

@Component
public class TransferMapper {

    public TransferDTO.Response.Transfer toResponse(Transfer transfer) {
        return new TransferDTO.Response.Transfer(
                transfer.getId(),
                transfer.getSenderId(),
                transfer.getReceiverId(),
                transfer.getAmount(),
                transfer.getCurrency(),
                transfer.getStatus()
        );
    }

    public Transfer toEntity(TransferDTO.Request.Transfer request) {
        return Transfer.builder()
                .senderId(request.senderId())
                .receiverId(request.receiverId())
                .amount(request.amount())
                .currency(request.currency())
                .build();
    }
}
