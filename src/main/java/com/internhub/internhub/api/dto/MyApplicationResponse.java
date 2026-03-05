package com.internhub.internhub.api.dto;

import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

public record MyApplicationResponse (
        Long applicationId,
        Long jobId,
        String jobTitle,
        String jobLocation,
        String status,
        LocalDateTime appliedAt
) {}
