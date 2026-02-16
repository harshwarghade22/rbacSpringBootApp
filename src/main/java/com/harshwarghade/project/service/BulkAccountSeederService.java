package com.harshwarghade.project.service;

import com.harshwarghade.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BulkAccountSeederService {

    private final JdbcTemplate jdbcTemplate;
    private final UserRepository userRepository;

    private static final int BATCH_SIZE = 1000;

    public String createAccountsForSingleUser(Long userId, int totalAccounts) {

        // Verify user exists
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String sql = "INSERT INTO accounts (account_number, account_type, balance, user_id) VALUES (?, ?, ?, ?)";

        long start = System.currentTimeMillis();
        int inserted = 0;

        for (int i = 0; i < totalAccounts; i += BATCH_SIZE) {

            int currentBatch = Math.min(BATCH_SIZE, totalAccounts - i);

            jdbcTemplate.batchUpdate(sql,
                    new org.springframework.jdbc.core.BatchPreparedStatementSetter() {

                        @Override
                        public void setValues(java.sql.PreparedStatement ps, int index) throws java.sql.SQLException {
                            String accountNumber = "ACC-" + UUID.randomUUID().toString().substring(0, 12);
                            String type = (index % 2 == 0) ? "SAVINGS" : "CURRENT";

                            ps.setString(1, accountNumber);
                            ps.setString(2, type);
                            ps.setDouble(3, 0.0);
                            ps.setLong(4, user.getId());
                        }

                        @Override
                        public int getBatchSize() {
                            return currentBatch;
                        }
                    });

            inserted += currentBatch;

            if (inserted % 500 == 0) {
                System.out.println("Accounts created: " + inserted);
            }
        }

        long end = System.currentTimeMillis();
        return "SUCCESS: Created " + inserted + " accounts in " + (end - start) + " ms";
    }
}
