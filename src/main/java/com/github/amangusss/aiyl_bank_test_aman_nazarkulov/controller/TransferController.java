package com.github.amangusss.aiyl_bank_test_aman_nazarkulov.controller;

import com.github.amangusss.aiyl_bank_test_aman_nazarkulov.aop.HttpLog;
import com.github.amangusss.aiyl_bank_test_aman_nazarkulov.dto.TransferDTO;
import com.github.amangusss.aiyl_bank_test_aman_nazarkulov.service.TransferService;

import jakarta.validation.Valid;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TransferController {

    TransferService transferService;

    @PostMapping("/transfers")
    @HttpLog
    public ResponseEntity<TransferDTO.Response.Transfer> executeTransfer(@Valid @RequestBody TransferDTO.Request.Transfer request) {

        return ResponseEntity.ok(transferService.sendTransfer(request));
    }
}
