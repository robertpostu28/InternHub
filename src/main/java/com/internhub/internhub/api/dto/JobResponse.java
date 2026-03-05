package com.internhub.internhub.api.dto;

import java.time.LocalDateTime;

public record JobResponse (
        Long id,
        String title,
        String location,
        String status,
        LocalDateTime createdAt
) {}
