package com.harshwarghade.project.service;

import com.harshwarghade.project.entity.*;
import com.harshwarghade.project.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.harshwarghade.project.dto.AdminAccountResponse;
import com.harshwarghade.project.dto.UserAccountResponse;

import java.util.stream.Collectors;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    // USER creates their own account
    public Account createAccountForUser(User user, AccountType type,String accountNumber) {

        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setAccountType(type);
        account.setBalance(0.0);
        account.setUser(user);

        return accountRepository.save(account);
    }

    // ADMIN creates account for any user
    public Account createAccountByAdmin(Long userId, AccountType type) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Account account = new Account();
        account.setAccountNumber(generateAccountNumber());
        account.setAccountType(type);
        account.setBalance(0.0);
        account.setUser(user);

        return accountRepository.save(account);
    }

    public List<AdminAccountResponse> getAllAccountsForAdmin() {

        List<Account> accounts = accountRepository.findAllWithUser();

        return accounts.stream()
                .map(account -> new AdminAccountResponse(
                        account.getAccountNumber(),
                        account.getUser().getId(),
                        account.getUser().getName()))
                .collect(Collectors.toList());
    }

    // USER: View only their accounts (SECURE + NO RECURSION)
    public List<UserAccountResponse> getAccountsByUser(Long userId) {

        List<Account> accounts = accountRepository.findByUserId(userId);

        return accounts.stream()
                .map(account -> new UserAccountResponse(
                        account.getAccountNumber(),
                        account.getAccountType().name(),
                        account.getBalance()))
                .collect(Collectors.toList());
    }

    private String generateAccountNumber() {
        return "ACC-" + UUID.randomUUID().toString().substring(0, 10);
    }
}
