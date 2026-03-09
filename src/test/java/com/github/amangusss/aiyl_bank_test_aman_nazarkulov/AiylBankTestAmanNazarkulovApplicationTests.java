package com.github.amangusss.aiyl_bank_test_aman_nazarkulov;

import com.github.amangusss.aiyl_bank_test_aman_nazarkulov.aop.LoggingAspect;
import com.github.amangusss.aiyl_bank_test_aman_nazarkulov.dto.TransferDTO;
import com.github.amangusss.aiyl_bank_test_aman_nazarkulov.entity.account.Account;
import com.github.amangusss.aiyl_bank_test_aman_nazarkulov.entity.account.AccountStatus;
import com.github.amangusss.aiyl_bank_test_aman_nazarkulov.entity.transfer.TransferStatus;
import com.github.amangusss.aiyl_bank_test_aman_nazarkulov.exception.CustomAccountNotFoundException;
import com.github.amangusss.aiyl_bank_test_aman_nazarkulov.exception.IllegalStatusStateException;
import com.github.amangusss.aiyl_bank_test_aman_nazarkulov.exception.IllegalTransferException;
import com.github.amangusss.aiyl_bank_test_aman_nazarkulov.repository.AccountRepository;
import com.github.amangusss.aiyl_bank_test_aman_nazarkulov.repository.TransferRepository;
import com.github.amangusss.aiyl_bank_test_aman_nazarkulov.service.TransferService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class AiylBankTestAmanNazarkulovApplicationTests {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private TransferService transferService;

    private Account senderAccount;
    private Account receiverAccount;

    @BeforeEach
    void setUp() {
        transferRepository.deleteAll();
        accountRepository.deleteAll();

        senderAccount = accountRepository.save(Account.builder()
                .name("Sender Account")
                .budget(1000.0)
                .status(AccountStatus.ACTIVE)
                .build());

        receiverAccount = accountRepository.save(Account.builder()
                .name("Receiver Account")
                .budget(500.0)
                .status(AccountStatus.ACTIVE)
                .build());
    }

    private void withTxId(String txId, Runnable action) {
        MDC.put(LoggingAspect.TRANSACTION_ID_KEY, txId);
        try {
            action.run();
        } finally {
            MDC.remove(LoggingAspect.TRANSACTION_ID_KEY);
        }
    }

    @Test
    @DisplayName("Context loads")
    void shouldLoadApplicationContext() {
        assertThat(transferService).isNotNull();
    }

    @Test
    @DisplayName("Successful transfer — deducts sender, credits receiver")
    void shouldDeductSenderAndCreditReceiverOnSuccessfulTransfer() {
        TransferDTO.Request.Transfer request = new TransferDTO.Request.Transfer(
                senderAccount.getId(), receiverAccount.getId(), 100.0, "USD");

        TransferDTO.Response.Transfer response = transferService.sendTransfer(request);

        assertThat(response.id()).isNotNull();
        assertThat(response.senderId()).isEqualTo(senderAccount.getId());
        assertThat(response.receiverId()).isEqualTo(receiverAccount.getId());
        assertThat(response.amount()).isEqualTo(100.0);
        assertThat(response.currency()).isEqualTo("USD");
        assertThat(response.status()).isEqualTo(TransferStatus.SUCCESS);

        Account updatedSender   = accountRepository.findById(senderAccount.getId()).orElseThrow();
        Account updatedReceiver = accountRepository.findById(receiverAccount.getId()).orElseThrow();
        assertThat(updatedSender.getBudget()).isEqualTo(900.0);
        assertThat(updatedReceiver.getBudget()).isEqualTo(600.0);
    }

    @Test
    @DisplayName("Insufficient funds — saves FAILED transfer")
    void shouldSaveFailedTransferWhenInsufficientFunds() {
        TransferDTO.Request.Transfer request = new TransferDTO.Request.Transfer(
                senderAccount.getId(), receiverAccount.getId(), 2000.0, "USD");

        withTxId("test-txn-2", () ->
                assertThatThrownBy(() -> transferService.sendTransfer(request))
                        .isInstanceOf(IllegalTransferException.class)
                        .hasMessageContaining("insufficient funds")
        );

        assertThat(transferRepository.findAll())
                .hasSize(1)
                .first()
                .satisfies(t -> assertThat(t.getStatus()).isEqualTo(TransferStatus.FAILED));
    }

    @Test
    @DisplayName("Inactive receiver — throws IllegalStatusStateException")
    void shouldThrowWhenReceiverIsInactive() {
        receiverAccount.setStatus(AccountStatus.INACTIVE);
        accountRepository.save(receiverAccount);

        withTxId("test-txn-3", () ->
                assertThatThrownBy(() -> transferService.sendTransfer(
                        new TransferDTO.Request.Transfer(senderAccount.getId(), receiverAccount.getId(), 100.0, "USD")))
                        .isInstanceOf(IllegalStatusStateException.class)
                        .hasMessageContaining("not active")
        );
    }

    @Test
    @DisplayName("Inactive sender — throws IllegalStatusStateException")
    void shouldThrowWhenSenderIsInactive() {
        senderAccount.setStatus(AccountStatus.INACTIVE);
        accountRepository.save(senderAccount);

        withTxId("test-txn-4", () ->
                assertThatThrownBy(() -> transferService.sendTransfer(
                        new TransferDTO.Request.Transfer(senderAccount.getId(), receiverAccount.getId(), 100.0, "USD")))
                        .isInstanceOf(IllegalStatusStateException.class)
                        .hasMessageContaining("not active")
        );
    }

    @Test
    @DisplayName("Sender not found — throws CustomAccountNotFoundException")
    void shouldThrowWhenSenderAccountNotFound() {
        withTxId("test-txn-5", () ->
                assertThatThrownBy(() -> transferService.sendTransfer(
                        new TransferDTO.Request.Transfer(99999L, receiverAccount.getId(), 100.0, "USD")))
                        .isInstanceOf(CustomAccountNotFoundException.class)
                        .hasMessageContaining("not found")
        );
    }

    @Test
    @DisplayName("Receiver not found — throws CustomAccountNotFoundException")
    void shouldThrowWhenReceiverAccountNotFound() {
        withTxId("test-txn-6", () ->
                assertThatThrownBy(() -> transferService.sendTransfer(
                        new TransferDTO.Request.Transfer(senderAccount.getId(), 99999L, 100.0, "USD")))
                        .isInstanceOf(CustomAccountNotFoundException.class)
                        .hasMessageContaining("not found")
        );
    }

    @Test
    @DisplayName("Same sender and receiver — throws IllegalTransferException")
    void shouldThrowWhenSenderAndReceiverAreTheSameAccount() {
        withTxId("test-txn-7", () ->
                assertThatThrownBy(() -> transferService.sendTransfer(
                        new TransferDTO.Request.Transfer(senderAccount.getId(), senderAccount.getId(), 100.0, "USD")))
                        .isInstanceOf(IllegalTransferException.class)
                        .hasMessageContaining("cannot be the same")
        );
    }

    @Test
    @DisplayName("Multiple sequential transfers — balances updated correctly")
    void shouldUpdateBalancesCorrectlyAfterMultipleSequentialTransfers() {
        TransferDTO.Response.Transfer first = transferService.sendTransfer(
                new TransferDTO.Request.Transfer(senderAccount.getId(), receiverAccount.getId(), 100.0, "USD"));
        assertThat(first.status()).isEqualTo(TransferStatus.SUCCESS);

        TransferDTO.Response.Transfer second = transferService.sendTransfer(
                new TransferDTO.Request.Transfer(senderAccount.getId(), receiverAccount.getId(), 200.0, "USD"));
        assertThat(second.status()).isEqualTo(TransferStatus.SUCCESS);

        Account finalSender   = accountRepository.findById(senderAccount.getId()).orElseThrow();
        Account finalReceiver = accountRepository.findById(receiverAccount.getId()).orElseThrow();
        assertThat(finalSender.getBudget()).isEqualTo(700.0);
        assertThat(finalReceiver.getBudget()).isEqualTo(800.0);
    }
}
