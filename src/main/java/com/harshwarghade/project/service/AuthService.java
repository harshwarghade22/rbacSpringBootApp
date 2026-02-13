package com.harshwarghade.project.service;

import com.harshwarghade.project.dto.AuthRequestDto;
import com.harshwarghade.project.dto.AuthResponseDto;
import com.harshwarghade.project.dto.RegisterRequestDto;
import com.harshwarghade.project.entity.Role;
import com.harshwarghade.project.entity.RoleName;
import com.harshwarghade.project.entity.User;
import com.harshwarghade.project.repository.RoleRepository;
import com.harshwarghade.project.repository.UserRepository;
import com.harshwarghade.project.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthResponseDto register(RegisterRequestDto request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        Set<Role> roleSet = new HashSet<>();

        for (String roleName : request.getRoles()) {
            RoleName roleEnum = RoleName.valueOf(roleName);
            Role role = roleRepository.findByRoleName(roleEnum)
                    .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
            roleSet.add(role);
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(roleSet);

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponseDto(token);
    }

    public AuthResponseDto login(AuthRequestDto request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        String token = jwtUtil.generateToken(request.getEmail());
        return new AuthResponseDto(token);
    }
}
