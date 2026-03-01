package com.internhub.internhub.api;

import com.internhub.internhub.api.dto.RegisterRequest;
import com.internhub.internhub.api.dto.UserResponse;
import com.internhub.internhub.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /*
        POST auth/register
        Public endpoint: creates a new user account with the provided email, password, full name, and role (CANDIDATE or RECRUITER).
        Password is hashed using BCrypt before being stored in the database.
    */
    @PostMapping("/register")
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.registerUser(request);
    }
}
