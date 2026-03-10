package com.internhub.internhub.api;

import com.internhub.internhub.api.dto.UserResponse;
import com.internhub.internhub.common.exception.NotFoundException;
import com.internhub.internhub.domain.User;
import com.internhub.internhub.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MeController {
    private final UserRepository userRepository;

    public MeController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public UserResponse me(Authentication auth) {
        String email = auth.getName(); // username is Spring Security = email in our case
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "User not found with email: " + email));

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().name()
        );
    }
}
