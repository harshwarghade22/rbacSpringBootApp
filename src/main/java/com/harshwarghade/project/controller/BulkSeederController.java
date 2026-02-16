package com.harshwarghade.project.controller;

import com.harshwarghade.project.service.BulkTransactionSeederService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/bulk")
@RequiredArgsConstructor
public class BulkSeederController {

    private final BulkTransactionSeederService seederService;

    @PostMapping("/transactions")
    @PreAuthorize("hasRole('ADMIN')")
    public String seedTransactions(@RequestParam int txnPerAccount) {
        return seederService.seedLargeTransactions(txnPerAccount);
    }
}
