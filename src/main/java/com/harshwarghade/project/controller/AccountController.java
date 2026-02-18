package com.harshwarghade.project.controller;

import com.harshwarghade.project.dto.UserAccountResponse;
import com.harshwarghade.project.entity.*;
import com.harshwarghade.project.repository.UserRepository;
import com.harshwarghade.project.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final UserRepository userRepository;

    // USER creates their own account
    // ADMIN can also call this (creates for self if admin logs in)
    @PostMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Account createMyAccount(
            @RequestParam AccountType type,
            Authentication authentication,String accountNumber) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return accountService.createAccountForUser(user, type,accountNumber);
    }

    // USER views only their accounts
    @GetMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public List<UserAccountResponse> getMyAccounts(Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return accountService.getAccountsByUser(user.getId());
    }
}
