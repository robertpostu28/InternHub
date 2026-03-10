package com.internhub.internhub.service;

import com.internhub.internhub.api.dto.RegisterRequest;
import com.internhub.internhub.api.dto.UserResponse;
import com.internhub.internhub.common.exception.ConflictException;
import com.internhub.internhub.domain.User;
import com.internhub.internhub.domain.enums.Role;
import com.internhub.internhub.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse registerUser(RegisterRequest registerRequest) {
        String email = registerRequest.email().trim().toLowerCase();

        // fast check (DB unique constraint is still the final safety net)
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("EMAIL_ALREADY_EXISTS", "Email already exists");
        }

        Role role;
        try {
            role = Role.valueOf(registerRequest.role().trim().toUpperCase());
        } catch (Exception e) {
            throw new ConflictException("INVALID_ROLE", "Role must be either CANDIDATE or RECRUITER");
        }

        User user = new User();
        user.setEmail(email);
        user.setFullName(registerRequest.fullName().trim());
        user.setRole(role);

        // Hash the password using BCrypt before saving to the database
        String passwordHash = passwordEncoder.encode(registerRequest.password());
        user.setPasswordHash(passwordHash);

        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        return new UserResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFullName(),
                savedUser.getRole().name()
        );
    }
}
