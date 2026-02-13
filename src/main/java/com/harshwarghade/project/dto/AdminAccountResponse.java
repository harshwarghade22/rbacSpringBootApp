package com.harshwarghade.project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminAccountResponse {

    private String accountNumber;
    private Long userId;
    private String userName;
}
