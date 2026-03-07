package com.internhub.internhub.api.dto;

public record TopJobDto (
        Long jobId,
        String title,
        String status,
        long applicationsCount
) {}
