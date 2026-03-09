package com.github.amangusss.aiyl_bank_test_aman_nazarkulov.repository;

import com.github.amangusss.aiyl_bank_test_aman_nazarkulov.entity.transfer.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, Long> {}
