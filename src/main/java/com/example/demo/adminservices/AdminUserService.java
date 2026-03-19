package com.example.demo.adminservices;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class AdminUserService {

    private final UserRepository userRepository;

    public AdminUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ==========================================
    // MODIFY USER ROLE (ADMIN <-> CUSTOMER)
    // ==========================================
    @Transactional
    public User modifyUser(Integer userId, String roleName) {

        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        if (roleName == null || roleName.isBlank()) {
            throw new IllegalArgumentException("Role name is required");
        }

        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        try {
            // Converts input to uppercase to match CUSTOMER or ADMIN enum constants
            Role newRole = Role.valueOf(roleName.trim().toUpperCase());
            
            // This logic allows two-way transition:
            // CUSTOMER -> ADMIN  (Allowed)
            // ADMIN -> CUSTOMER  (Allowed)
            existingUser.setRole(newRole);
            
        } catch (IllegalArgumentException e) {
            // Updated error message to reflect your actual roles
            throw new IllegalArgumentException("Invalid role: " + roleName + ". Accepted values are ADMIN or CUSTOMER.");
        }

        existingUser.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(existingUser);
    }

    // =========================
    // GET USER BY ID
    // =========================
    public User getUserById(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
    }

    // =========================
    // DELETE USER BY ID
    // =========================
    @Transactional
    public void deleteUserById(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        
        userRepository.delete(existingUser);
    }
}