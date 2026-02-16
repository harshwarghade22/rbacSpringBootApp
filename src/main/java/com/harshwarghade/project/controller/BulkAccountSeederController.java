package com.harshwarghade.project.controller;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.harshwarghade.project.service.BulkAccountSeederService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/bulk")
@RequiredArgsConstructor
public class BulkAccountSeederController {

    private final BulkAccountSeederService service;

    @PostMapping("/accounts-single-user")
    @PreAuthorize("hasRole('ADMIN')")
    public String createAccounts(
            @RequestParam Long userId,
            @RequestParam int totalAccounts) {

        return service.createAccountsForSingleUser(userId, totalAccounts);
    }
}
