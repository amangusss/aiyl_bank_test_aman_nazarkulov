package com.github.amangusss.aiyl_bank_test_aman_nazarkulov.service.impl;

import com.github.amangusss.aiyl_bank_test_aman_nazarkulov.aop.LoggingAspect;
import com.github.amangusss.aiyl_bank_test_aman_nazarkulov.dto.TransferDTO;
import com.github.amangusss.aiyl_bank_test_aman_nazarkulov.entity.account.Account;
import com.github.amangusss.aiyl_bank_test_aman_nazarkulov.entity.account.AccountStatus;
import com.github.amangusss.aiyl_bank_test_aman_nazarkulov.entity.transfer.Transfer;
import com.github.amangusss.aiyl_bank_test_aman_nazarkulov.entity.transfer.TransferStatus;
import com.github.amangusss.aiyl_bank_test_aman_nazarkulov.event.TransferFailedEvent;
import com.github.amangusss.aiyl_bank_test_aman_nazarkulov.exception.CustomAccountNotFoundException;
import com.github.amangusss.aiyl_bank_test_aman_nazarkulov.exception.IllegalStatusStateException;
import com.github.amangusss.aiyl_bank_test_aman_nazarkulov.exception.IllegalTransferException;
import com.github.amangusss.aiyl_bank_test_aman_nazarkulov.mapper.TransferMapper;
import com.github.amangusss.aiyl_bank_test_aman_nazarkulov.repository.AccountRepository;
import com.github.amangusss.aiyl_bank_test_aman_nazarkulov.repository.TransferRepository;
import com.github.amangusss.aiyl_bank_test_aman_nazarkulov.service.TransferService;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TransferServiceImpl implements TransferService {

    TransferRepository transferRepository;
    AccountRepository accountRepository;
    TransferMapper transferMapper;
    ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public TransferDTO.Response.Transfer sendTransfer(TransferDTO.Request.Transfer request) {
        String txId = resolveTransactionId();

        log.info("[Transaction: {}] Starting transfer: accountId {} -> accountId {}, amount {}",
                txId, request.senderId(), request.receiverId(), request.amount());

        Transfer transfer = transferMapper.toEntity(request);
        transfer.setCreatedAt(LocalDateTime.now());

        try {
            Account sender = findAccount(request.senderId(), "Sender");
            Account receiver = findAccount(request.receiverId(), "Receiver");

            validate(sender, receiver, transfer);
            executeTransfer(sender, receiver, transfer);

            log.info("[Transaction: {}] Transfer completed successfully", txId);
            return transferMapper.toResponse(transfer);
        } catch (Exception ex) {
            log.error("[Transaction: {}] Transfer failed: {}", txId, ex.getMessage());
            eventPublisher.publishEvent(new TransferFailedEvent(transfer));
            throw ex;
        }
    }

    private String resolveTransactionId() {
        String txId = MDC.get(LoggingAspect.TRANSACTION_ID_KEY);
        if (txId == null) {
            txId = UUID.randomUUID().toString();
            MDC.put(LoggingAspect.TRANSACTION_ID_KEY, txId);
        }
        return txId;
    }

    private Account findAccount(Long accountId, String role) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new CustomAccountNotFoundException(
                        role + " account id " + accountId + " not found"));
    }

    private void validate(Account sender, Account receiver, Transfer transfer) {
        if (!sender.getStatus().equals(AccountStatus.ACTIVE)) {
            throw new IllegalStatusStateException("Sender account id " + sender.getId() + " is not active");
        }

        if (!receiver.getStatus().equals(AccountStatus.ACTIVE)) {
            throw new IllegalStatusStateException("Receiver account id " + receiver.getId() + " is not active");
        }

        if (sender.getId().equals(receiver.getId())) {
            throw new IllegalTransferException("Sender and receiver accounts cannot be the same");
        }

        if (sender.getBudget() < transfer.getAmount()) {
            throw new IllegalTransferException("Sender account id " + sender.getId() + " has insufficient funds"
                    + " for transfer amount " + transfer.getAmount()
                    + ". Current budget: " + sender.getBudget());
        }
    }

    private void executeTransfer(Account sender, Account receiver, Transfer transfer) {
        sender.setBudget(sender.getBudget() - transfer.getAmount());
        receiver.setBudget(receiver.getBudget() + transfer.getAmount());
        transfer.setStatus(TransferStatus.SUCCESS);

        accountRepository.save(sender);
        accountRepository.save(receiver);
        transferRepository.save(transfer);
    }
}
