// import org.springframework.boot.autoconfigure.neo4j.Neo4jProperties.Authentication;
package com.harshwarghade.project.controller;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.harshwarghade.project.entity.Account;
import com.harshwarghade.project.entity.User;
import com.harshwarghade.project.repository.AccountRepository;
import com.harshwarghade.project.repository.UserRepository;
import com.harshwarghade.project.service.AccountTransactionExportService;
import org.springframework.security.core.Authentication;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user/export")
@RequiredArgsConstructor
public class TransactionExportController {

    private final AccountTransactionExportService exportService;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @GetMapping("/transactions/{accountId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public String exportTransactionsForAccount(
            @PathVariable Long accountId,
            Authentication authentication) throws Exception {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        // 🔐 Security: User can export only their own account
        boolean isOwner = account.getUser().getId().equals(user.getId());
        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isOwner && !isAdmin) {
            throw new RuntimeException("Access denied for this account export");
        }

        return exportService.exportSingleAccountTransactions(accountId);
    }
}
