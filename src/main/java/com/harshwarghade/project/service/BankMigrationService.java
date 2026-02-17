package com.harshwarghade.project.service;

import com.harshwarghade.project.dto.BankTxn;
import com.harshwarghade.project.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class BankMigrationService {

    private final RestTemplate restTemplate;
    private final JdbcTemplate jdbcTemplate;

    private static final String BANK_API_URL =
            "http://localhost:8081/api/bank/transactions?page=%d&size=%d";

    // 🔥 Optimized for local machine + large dataset
    private static final int PAGE_SIZE = 20000;   // reduces API calls drastically
    private static final int BATCH_SIZE = 5000;   // optimal JDBC batch for MySQL
    private static final int THREAD_COUNT = 4;    // safe default for local CPU

    // Restart-safe (skips duplicates due to UNIQUE source_txn_id)
    private static final String INSERT_SQL =
            "INSERT IGNORE INTO transactions_copy " +
            "(amount, timestamp, type, account_id, source_txn_id) " +
            "VALUES (?, ?, ?, ?, ?)";

    public void migrateAllTransactions() throws InterruptedException {

        long startTime = System.currentTimeMillis();
        AtomicLong totalMigrated = new AtomicLong(0);

        log.info("🚀 Starting MULTI-THREADED Bank Migration...");
        log.info("Page Size: {} | Batch Size: {} | Threads: {}",
                PAGE_SIZE, BATCH_SIZE, THREAD_COUNT);

        // Step 1: Fetch first page to get totalPages
        PageResponse<BankTxn> firstPage = fetchPage(0);

        if (firstPage == null || firstPage.getContent() == null) {
            log.error("Failed to fetch first page from Bank API. Aborting.");
            return;
        }

        int totalPages = firstPage.getTotalPages();
        log.info("Total Pages to Process: {}", totalPages);

        // Step 2: Create thread pool
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

        // Step 3: Submit all page tasks (parallel execution)
        for (int page = 0; page < totalPages; page++) {
            final int currentPage = page;

            executor.submit(() -> {
                try {
                    processPage(currentPage, totalMigrated);
                } catch (Exception e) {
                    log.error("Error processing page {}", currentPage, e);
                }
            });
        }

        // Step 4: Shutdown and wait for completion
        executor.shutdown();
        boolean finished = executor.awaitTermination(12, TimeUnit.HOURS);

        long totalTimeSec = (System.currentTimeMillis() - startTime) / 1000;

        if (finished) {
            log.info("🎉 MIGRATION COMPLETED SUCCESSFULLY!");
            log.info("Total Records Migrated: {}", totalMigrated.get());
            log.info("Total Time Taken: {} seconds (~{} minutes)",
                    totalTimeSec, totalTimeSec / 60);
        } else {
            log.error("Migration did not finish within expected time!");
        }
    }

    private void processPage(int page, AtomicLong totalMigrated) {

        long pageStart = System.currentTimeMillis();

        PageResponse<BankTxn> pageData = fetchPage(page);

        if (pageData == null || pageData.getContent() == null || pageData.getContent().isEmpty()) {
            log.warn("Page {} returned empty data.", page);
            return;
        }

        var transactions = pageData.getContent();

        // 🔥 Ultra-fast JDBC batch insert (no Hibernate overhead)
        jdbcTemplate.batchUpdate(
                INSERT_SQL,
                transactions,
                BATCH_SIZE,
                (ps, txn) -> {
                    ps.setDouble(1, txn.getAmount());
                    ps.setObject(2, txn.getTimestamp());
                    ps.setString(3, txn.getType());
                    ps.setLong(4, txn.getAccountId());
                    ps.setLong(5, txn.getId()); // source_txn_id for dedup safety
                }
        );

        long migrated = totalMigrated.addAndGet(transactions.size());
        long pageTimeSec = (System.currentTimeMillis() - pageStart) / 1000;

        // Reduce logging overhead (log every 10 pages)
        if (page % 10 == 0) {
            log.info("Page {} done | Records: {} | Page Time: {} sec | Total Migrated: {}",
                    page, transactions.size(), pageTimeSec, migrated);
        }
    }

    private PageResponse<BankTxn> fetchPage(int page) {
        String url = String.format(BANK_API_URL, page, PAGE_SIZE);

        ResponseEntity<PageResponse<BankTxn>> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<PageResponse<BankTxn>>() {}
                );

        return response.getBody();
    }
}
