package com.internhub.internhub.api;

import com.internhub.internhub.api.dto.JobCreateRequest;
import com.internhub.internhub.api.dto.JobResponse;
import com.internhub.internhub.service.JobService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.internhub.internhub.api.dto.JobDetailsResponse;

@RestController
@RequestMapping("/jobs")
public class JobController {
    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    // Recruiter-only endpoint to create a new job posting. The authenticated user's email is used to associate the job with the recruiter.
    @PreAuthorize("hasRole('RECRUITER')")
    @PostMapping
    public JobResponse create(@Valid @RequestBody JobCreateRequest request, Authentication auth) {
        return jobService.createJob(auth.getName(), request);
    }

    // Public feed: list OPEN jobs with pagination
    @GetMapping
    public Page<JobResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return jobService.listOpenJobs(page, size);
    }

    @GetMapping("/{id}")
    public JobDetailsResponse getById(@PathVariable Long id) {
        return jobService.getJobById(id);
    }

    @PreAuthorize("hasRole('RECRUITER')")
    @GetMapping("/recruiter")
    public Page<JobResponse> recruiterJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication auth
    ) {
        return jobService.listRecruiterJobs(auth.getName(), page, size);
    }

    @PreAuthorize("hasRole('RECRUITER')")
    @PatchMapping("{id}/close")
    public JobResponse close(@PathVariable Long id, Authentication auth) {
        return jobService.closeJob(id, auth.getName());
    }
}
