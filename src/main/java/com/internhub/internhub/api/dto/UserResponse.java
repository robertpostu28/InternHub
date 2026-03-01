package com.internhub.internhub.api.dto;

public record UserResponse (
        Long id,
        String email,
        String fullName,
        String role
) {}
