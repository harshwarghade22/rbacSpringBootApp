package com.harshwarghade.project.controller;

import com.harshwarghade.project.dto.BankTransactionDTO;
import com.harshwarghade.project.entity.Account;
import com.harshwarghade.project.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/api/bank")
@RequiredArgsConstructor
public class MockBankController {

    private final AccountRepository accountRepository;
    private final Random random = new Random();

    // Simulates Bank Server API
    @GetMapping("/transactions")
    public List<BankTransactionDTO> getBankTransactions(
            @RequestParam(required = false) LocalDate date) {

        List<Account> accounts = accountRepository.findAll();
        List<BankTransactionDTO> transactions = new ArrayList<>();

        for (Account account : accounts) {

            // Generate 5 dummy transactions per account (simulating bank data)
            for (int i = 0; i < 5; i++) {

                double amount = 100 + random.nextInt(1000);
                String type = random.nextBoolean() ? "DEPOSIT" : "WITHDRAW";

                transactions.add(new BankTransactionDTO(
                        account.getAccountNumber(),
                        amount,
                        type,
                        LocalDateTime.now().minusHours(random.nextInt(24))
                ));
            }
        }

        return transactions;
    }
}
