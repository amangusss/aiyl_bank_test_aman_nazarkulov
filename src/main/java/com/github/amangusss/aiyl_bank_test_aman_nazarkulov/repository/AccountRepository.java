package com.github.amangusss.aiyl_bank_test_aman_nazarkulov.repository;

import com.github.amangusss.aiyl_bank_test_aman_nazarkulov.entity.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {}
