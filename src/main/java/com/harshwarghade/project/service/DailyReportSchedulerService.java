package com.harshwarghade.project.service;

import com.harshwarghade.project.entity.Account;
import com.harshwarghade.project.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DailyReportSchedulerService {

    private final AccountRepository accountRepository;
    private final AccountTransactionExportService exportService;

    // Runs every day at 9:00 AM IST
    // @Scheduled(cron = "0 0 9 * * *")
    // @Scheduled(cron = "0 */1 * * * *") // Every 1 minute
    @Async
    public void generateDailyTransactionReports() {

        log.info("CRON STARTED: Daily Report Generation at {}", LocalDateTime.now());

        List<Account> accounts = accountRepository.findAll();
        log.info("Total Accounts to Process: {}", accounts.size());

        int successCount = 0;
        int failedCount = 0;

        for (Account account : accounts) {
            try {
                Long accountId = account.getId();

                log.info("Generating report for Account ID: {}", accountId);

                
                String s3Url = exportService.exportSingleAccountTransactions(accountId);

                log.info("Report uploaded to S3 for Account {}: {}", accountId, s3Url);

                successCount++;

            } catch (Exception e) {
                failedCount++;
                log.error("Failed to generate report for Account ID: {}", account.getId(), e);
            }
        }

        log.info("CRON COMPLETED: Reports Generated = {}, Failed = {}",
                successCount, failedCount);
    }
}
