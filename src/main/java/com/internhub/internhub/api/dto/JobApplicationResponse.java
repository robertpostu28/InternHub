package com.internhub.internhub.api.dto;

import java.time.LocalDateTime;

public record JobApplicationResponse (
        Long id,
        Long candidateId,
        String candidateEmail,
        String candidateFullName,
        String status,
        LocalDateTime appliedAt
) {}
