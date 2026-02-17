package com.harshwarghade.project.service;

import com.harshwarghade.project.client.BankApiClient;
import com.harshwarghade.project.dto.BankTxn;
import com.harshwarghade.project.dto.PageResponse;
import com.harshwarghade.project.entity.Account;
import com.harshwarghade.project.entity.Transaction;
import com.harshwarghade.project.entity.TransactionType;
import com.harshwarghade.project.repository.AccountRepository;
import com.harshwarghade.project.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BankTransactionSyncService {

    private final BankApiClient bankApiClient;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    private static final int PAGE_SIZE = 5000;

    public void migrateAllTransactions() {

        log.info("🚀 Starting 2M transaction migration from Bank API...");

        // 🔥 ULTRA IMPORTANT: Load accounts ONCE (only 1 DB query)
        Map<Long, Account> accountMap = accountRepository.findAll()
                .stream()
                .collect(Collectors.toMap(
                        Account::getId,
                        acc -> acc));

        log.info("Loaded {} accounts into memory cache", accountMap.size());

        int page = 0;
        boolean last = false;
        long totalSaved = 0;

        while (!last) {

            PageResponse<BankTxn> response = bankApiClient.fetchTransactions(page, PAGE_SIZE);

            if (response == null || response.getContent().isEmpty()) {
                log.info("No more transactions received from Bank API");
                break;
            }

            List<Transaction> batch = new ArrayList<>(response.getContent().size());

            for (BankTxn dto : response.getContent()) {

                // ⚡ O(1) lookup (VERY FAST)
                Account account = accountMap.get(dto.getAccountId());

                if (account == null) {
                    log.warn("Account not found for accountId: {}", dto.getAccountId());
                    continue;
                }

                Transaction txn = new Transaction();
                txn.setAccount(account); // ✅ Correct JPA mapping
                txn.setAmount(dto.getAmount());
                txn.setTimestamp(dto.getTimestamp());
                txn.setType(TransactionType.valueOf(dto.getType()));

                batch.add(txn);
            }

            // Bulk insert (efficient for large data)
            transactionRepository.saveAll(batch);
            totalSaved += batch.size();

            log.info("Page {} completed | Records this page: {} | Total migrated: {}",
                    page, response.getContent().size(), totalSaved);

            last = response.isLast();
            page++;
        }

        log.info("🎉 Migration COMPLETED! Total Transactions Migrated: {}", totalSaved);
    }
}
