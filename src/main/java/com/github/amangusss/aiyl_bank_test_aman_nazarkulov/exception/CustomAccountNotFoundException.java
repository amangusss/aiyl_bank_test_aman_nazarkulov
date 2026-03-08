package com.github.amangusss.aiyl_bank_test_aman_nazarkulov.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomAccountNotFoundException extends BaseException {
    public CustomAccountNotFoundException(String message) {
        super(message);
    }
}
