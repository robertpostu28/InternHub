package com.internhub.internhub.api.dto;

import java.time.LocalDateTime;

public record JobDetailsResponse (
        Long id,
        String title,
        String description,
        String requirements,
        String location,
        String status,
        Long recruiterId,
        LocalDateTime createdAt
) {}
