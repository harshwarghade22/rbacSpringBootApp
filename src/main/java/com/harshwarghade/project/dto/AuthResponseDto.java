package com.harshwarghade.project.dto;

import java.util.Set;

import com.harshwarghade.project.entity.Role;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponseDto {
    private String token;
    private Set<String> roles;
    private String name;
}
