package com.internhub.internhub.api.dto;

import java.util.List;

public record JobStatusBreakdownDto(
        Long jobId,
        String title,
        String status,
        List<StatusCountDto> counts
) {}