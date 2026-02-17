package com.harshwarghade.project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BankTransactionDTO {

    private String accountNumber;
    private Double amount;
    private String type;
    private LocalDateTime timestamp;
}
