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
import lombok.extern.slf4j.Slf4j;

// import org.hibernate.engine.jdbc.env.internal.logger;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
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
        boolean isAdminRequested = false;
        for (String roleName : request.getRoles()) {
            RoleName roleEnum = RoleName.valueOf(roleName);
            if (roleEnum == RoleName.ROLE_ADMIN) {
                isAdminRequested = true;
            }
            Role role = roleRepository.findByRoleName(roleEnum)
                    .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
            roleSet.add(role);
        }

        // If admin role is requested, check if current user is admin (except for first admin)
        if (isAdminRequested) {
            long adminCount = userRepository.findAll().stream()
                    .filter(u -> u.getRoles().stream().anyMatch(r -> r.getRoleName() == RoleName.ROLE_ADMIN))
                    .count();
            if (adminCount > 0) {
                // Not the first admin, require current user to be admin
                org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                log.info("Admin registration requested. Current authentication: {}", authentication);
                if (authentication == null || !authentication.isAuthenticated()) {
                    throw new RuntimeException("Only authenticated admin can register a new admin");
                }
                boolean isCurrentUserAdmin = authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                if (!isCurrentUserAdmin) {
                    throw new RuntimeException("Only admin can register a new admin");
                }
            }
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(roleSet);

        userRepository.save(user);

        User savedUser = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found after registration"));

        String token = jwtUtil.generateToken(savedUser);
            Set<String> roleNames = user.getRoles().stream()
                    .map(r -> r.getRoleName().name())
                    .collect(java.util.stream.Collectors.toSet());

        String name = savedUser.getName();
            return new AuthResponseDto(token, roleNames, name);
    }

    public AuthResponseDto login(AuthRequestDto request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateToken(user);
            Set<String> roleNames = user.getRoles().stream()
                .map(r -> r.getRoleName().name())
                .collect(java.util.stream.Collectors.toSet());

        String name= user.getName();
            return new AuthResponseDto(token, roleNames, name);
    }
}
