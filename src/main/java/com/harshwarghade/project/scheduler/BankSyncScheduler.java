package com.harshwarghade.project.scheduler;

import com.harshwarghade.project.service.BankMigrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BankSyncScheduler {

    private final BankMigrationService bankMigrationService; // ✅ CORRECT SERVICE

    @Async
    // @Scheduled(initialDelay = 15000, fixedDelay = Long.MAX_VALUE)
    public void migrateTransactionsFromBankServer() throws InterruptedException {

        log.info("CRON STARTED: Migrating into transactions_copy table");

        bankMigrationService.migrateAllTransactions(); // ✅ NOT syncService

        log.info("CRON COMPLETED: Migration into transactions_copy finished");
    }
}
