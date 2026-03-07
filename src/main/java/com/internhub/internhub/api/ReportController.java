package com.internhub.internhub.api;

import com.internhub.internhub.api.dto.JobStatusBreakdownDto;
import com.internhub.internhub.api.dto.RecruiterOverviewDto;
import com.internhub.internhub.api.dto.TopJobDto;
import com.internhub.internhub.service.ReportService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reports")
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    // Endpoint for recruiters to get an overview of their job postings and applications
    @GetMapping("/recruiter/overview")
    @PreAuthorize("hasRole('RECRUITER')")
    public RecruiterOverviewDto overview(Authentication authentication) {
        return reportService.recruiterOverview(authentication.getName());
    }

    // Endpoint for recruiters to get a list of their top job postings based on application count
    @GetMapping("/recruiter/top-jobs")
    @PreAuthorize("hasRole('RECRUITER')")
    public List<TopJobDto> topJobs(Authentication authentication,
                                   @RequestParam(defaultValue = "5") int limit
    ) {
        return reportService.topJobs(authentication.getName(), limit);
    }

    // Endpoint for recruiters to get a breakdown of application statuses for a specific job posting
    @GetMapping("/jobs/{jobId}/status-breakdown")
    @PreAuthorize("hasRole('RECRUITER')")
    public JobStatusBreakdownDto jobStatusBreakdown(Authentication authentication,
                                                    @PathVariable Long jobId
    ) {
        return reportService.jobStatusBreakdown(authentication.getName(), jobId);
    }
}
