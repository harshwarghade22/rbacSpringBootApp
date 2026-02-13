package com.harshwarghade.project.dto;

import java.util.Set;

import lombok.Data;

@Data
public class RegisterRequestDto {
    private String email;
    private String password;
    private String name;
    private Set<String> roles;
}
