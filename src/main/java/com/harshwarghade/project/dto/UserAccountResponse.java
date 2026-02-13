package com.harshwarghade.project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserAccountResponse {

    private String accountNumber;
    private String accountType;
    private Double balance;
}
