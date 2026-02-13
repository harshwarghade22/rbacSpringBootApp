package com.harshwarghade.project.controller;

import com.harshwarghade.project.dto.AdminAccountResponse;
import com.harshwarghade.project.entity.Account;
import com.harshwarghade.project.entity.AccountType;
import com.harshwarghade.project.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/accounts")
@RequiredArgsConstructor
public class AdminAccountController {

    private final AccountService accountService;

    // 🔒 ADMIN: View all accounts (ONLY accountNumber + userId + userName)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminAccountResponse> getAllAccounts() {
        return accountService.getAllAccountsForAdmin();
    }

    // ADMIN can create account for any user
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Account createAccountForUser(
            @RequestParam Long userId,
            @RequestParam AccountType type) {

        return accountService.createAccountByAdmin(userId, type);
    }
}
