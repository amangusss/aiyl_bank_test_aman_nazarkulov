package com.github.amangusss.aiyl_bank_test_aman_nazarkulov.event;

import com.github.amangusss.aiyl_bank_test_aman_nazarkulov.entity.transfer.Transfer;
import com.github.amangusss.aiyl_bank_test_aman_nazarkulov.entity.transfer.TransferStatus;
import com.github.amangusss.aiyl_bank_test_aman_nazarkulov.repository.TransferRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransferFailedEventListener {

    private final TransferRepository transferRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onTransferFailed(TransferFailedEvent event) {
        Transfer transfer = event.transfer();

        if (transfer.getSenderId() == null) {
            return;
        }

        transfer.setStatus(TransferStatus.FAILED);

        Transfer saved = transferRepository.saveAndFlush(transfer);
        log.info("Saved FAILED transfer id={}: senderId={}, receiverId={}, amount={}",
                saved.getId(), saved.getSenderId(), saved.getReceiverId(), saved.getAmount());
    }
}
