package com.github.amangusss.aiyl_bank_test_aman_nazarkulov.entity.account;

import lombok.Getter;

@Getter
public enum AccountStatus {
    ACTIVE("ACTIVE"),
    INACTIVE("INACTIVE");

    private final String status;

    AccountStatus(String status) {
        this.status = status;
    }
}
