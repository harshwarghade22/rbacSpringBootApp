package com.harshwarghade.project.service;

import com.harshwarghade.project.entity.Transaction;
import com.harshwarghade.project.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionSchedulerService {

    private final TransactionRepository transactionRepository;

    private static final int PAGE_SIZE = 5000; // Safe for large datasets

    // Runs every day at 9:00 AM IST
    // @Scheduled(cron = "0 */1 * * * *")
    public void fetchDailyTransactions() {

        log.info("CRON STARTED: Fetching transactions at {}", LocalDateTime.now());

        int page = 0;
        long totalFetched = 0;

        while (true) {
            Page<Transaction> transactionPage =
                    transactionRepository.findAll(PageRequest.of(page, PAGE_SIZE));

            if (transactionPage.isEmpty()) {
                break;
            }

            totalFetched += transactionPage.getNumberOfElements();

            log.info("Fetched page {} with {} transactions",
                    page,
                    transactionPage.getNumberOfElements());

            // 🔥 Future processing logic can go here
            // Example:
            // - analytics
            // - export
            // - cache
            // - reconciliation

            page++;
        }

        log.info("CRON COMPLETED: Total Transactions Fetched = {}", totalFetched);
    }
}
