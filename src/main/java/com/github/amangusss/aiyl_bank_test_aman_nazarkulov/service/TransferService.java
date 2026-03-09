package com.github.amangusss.aiyl_bank_test_aman_nazarkulov.service;

import com.github.amangusss.aiyl_bank_test_aman_nazarkulov.dto.TransferDTO;

public interface TransferService {

    TransferDTO.Response.Transfer sendTransfer(TransferDTO.Request.Transfer request);
}
