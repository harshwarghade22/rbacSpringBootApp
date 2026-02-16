package com.harshwarghade.project.service;

import com.harshwarghade.project.entity.Account;
import com.harshwarghade.project.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class BulkTransactionSeederService {

    private final JdbcTemplate jdbcTemplate;
    private final AccountRepository accountRepository;

    private static final int BATCH_SIZE = 5000; // OPTIMAL for 16GB RAM

    public String seedLargeTransactions(int transactionsPerAccount) {

        List<Account> accounts = accountRepository.findAll();

        if (accounts.isEmpty()) {
            throw new RuntimeException("No accounts found");
        }

        Random random = new Random();

        String sql = "INSERT INTO project_db.transactions (amount, type, timestamp, account_id) VALUES (?, ?, ?, ?)";

        long totalInserted = 0;
        long startTime = System.currentTimeMillis();

        for (Account account : accounts) {

            for (int i = 0; i < transactionsPerAccount; i += BATCH_SIZE) {

                int currentBatchSize = Math.min(BATCH_SIZE, transactionsPerAccount - i);

                jdbcTemplate.batchUpdate(sql,
                        new org.springframework.jdbc.core.BatchPreparedStatementSetter() {

                            @Override
                            public void setValues(PreparedStatement ps, int index) throws java.sql.SQLException {

                                double amount = 10 + (1000 - 10) * random.nextDouble();
                                String type = random.nextBoolean() ? "DEPOSIT" : "WITHDRAW";

                                ps.setDouble(1, amount);
                                ps.setString(2, type);
                                ps.setObject(3, LocalDateTime.now());
                                ps.setLong(4, account.getId());
                            }

                            

                            @Override
                            public int getBatchSize() {
                                return currentBatchSize;
                            }
                        });
                
                

                totalInserted += currentBatchSize;
                

                // Progress log (VERY USEFUL)
                if (totalInserted % 100000 == 0) {
                    System.out.println("Inserted: " + totalInserted + " transactions...");
                }
            }
        }

        long endTime = System.currentTimeMillis();
        long duration = (endTime - startTime) / 1000;

        return "SUCCESS: Inserted " + totalInserted + " transactions in " + duration + " seconds";
    }
}
