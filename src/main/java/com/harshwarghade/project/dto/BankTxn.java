package com.harshwarghade.project.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BankTxn {
    private Long id;
    private Double amount;
    private LocalDateTime timestamp;
    private String type;
    private Long accountId;
    private String accountNumber;  
}
