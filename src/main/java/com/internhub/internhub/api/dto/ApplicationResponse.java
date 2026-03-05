package com.internhub.internhub.api.dto;

import java.time.LocalDateTime;

public record ApplicationResponse (
        Long id,
        Long jobId,
        Long candidateId,
        String status,
        LocalDateTime appliedAt
) {}
