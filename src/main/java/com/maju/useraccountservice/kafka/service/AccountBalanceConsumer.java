package com.maju.useraccountservice.kafka.service;

import com.maju.useraccountservice.entity.Account;
import com.maju.useraccountservice.enums.transaction.TransactionDirection;
import com.maju.useraccountservice.exceptions.NotFoundException;
import com.maju.useraccountservice.kafka.dto.BalanceUpdateEvent;
import com.maju.useraccountservice.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountBalanceConsumer {

    private final AccountRepository accountRepository;

    private final AccountEventPublisher accountEventPublisher;

    @KafkaListener(topics = "balance-update-events", groupId = "account-group")
    @Transactional
    public void consumerBalanceUpdate(BalanceUpdateEvent event) {

        log.info("processing balance update for account number: {}", event.getAccountNumber());

        Account account = accountRepository.findByAccountNumber(event.getAccountNumber())
                .orElseThrow(() -> new NotFoundException("account number not found"));

        //update balance of the account
        if (event.getTransactionDirection() == TransactionDirection.CREDIT) {
            account.setBalance(account.getBalance().add(event.getAmount()));
        } else if (event.getTransactionDirection() == TransactionDirection.DEBIT) {
            account.setBalance(account.getBalance().subtract(event.getAmount()));
        }

        accountRepository.save(account);

        BalanceUpdateEvent balanceUpdateEventToPublishToNotification = BalanceUpdateEvent.builder()
                .email(account.getUser().getEmail())
                .firstName(account.getUser().getFirsName())
                .accountNumber(account.getAccountNumber())
                .amount(event.getAmount())
                .description(event.getDescription())
                .transactionDirection(event.getTransactionDirection())
                .reference(event.getReference())
                .currentBalance(account.getBalance())
                .build();

        accountEventPublisher.publishTransactionNotificationEvent(balanceUpdateEventToPublishToNotification);
    }
}
