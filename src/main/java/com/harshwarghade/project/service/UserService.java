package com.harshwarghade.project.service;

import com.harshwarghade.project.dto.UserDto;
import com.harshwarghade.project.entity.User;
import com.harshwarghade.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // 🔥 Cache GET ALL USERS (DTO instead of Entity)
    @Cacheable(value = "users")
    public List<UserDto> getAllUsers() {
        System.out.println("Fetching users from DATABASE...");

        return userRepository.findAll()
                .stream()
                .map(user -> new UserDto(
                        user.getId(),
                        user.getName(),
                        user.getEmail()
                ))
                .toList();
    }

    // Cache single user (DTO)
    @Cacheable(value = "user", key = "#id")
    public UserDto getUserById(Long id) {
        System.out.println("Fetching user from DATABASE...");

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

    // When updating, clear cache
    @CacheEvict(value = {"users", "user"}, allEntries = true)
    public UserDto updateUser(Long id, User updatedUser) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setName(updatedUser.getName());
        user.setEmail(updatedUser.getEmail());

        User savedUser = userRepository.save(user);

        return new UserDto(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail()
        );
    }

    // When deleting, clear cache
    @CacheEvict(value = {"users", "user"}, allEntries = true)
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
