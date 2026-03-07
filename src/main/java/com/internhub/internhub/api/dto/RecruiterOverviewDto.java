package com.internhub.internhub.api.dto;

import java.util.List;

public record RecruiterOverviewDto(
        long totalJobs,
        long openJobs,
        long closedJobs,
        long totalApplications,
        List<StatusCountDto> applicationsByStatus
) {}