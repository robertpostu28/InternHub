package com.internhub.internhub.api;

import com.internhub.internhub.api.dto.ApplicationResponse;
import com.internhub.internhub.service.ApplicationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.internhub.internhub.api.dto.JobApplicationResponse;
import org.springframework.data.domain.Page;

import com.internhub.internhub.api.dto.UpdateApplicationStatusRequest;
import jakarta.validation.Valid;

import com.internhub.internhub.api.dto.MyApplicationResponse;

@RestController
@RequestMapping()
public class ApplicationController {
    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PreAuthorize("hasRole('CANDIDATE')")
    @PostMapping("/jobs/{jobId}/apply")
    public ApplicationResponse apply(@PathVariable Long jobId, Authentication auth) {
        return applicationService.apply(auth.getName(), jobId);
    }

    @PreAuthorize("hasRole('RECRUITER')")
    @GetMapping("/jobs/{jobId}/applications")
    public Page<JobApplicationResponse> listForJob(
            @PathVariable Long jobId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication auth
    ) {
        return applicationService.listApplicationsForJob(jobId, auth.getName(), page, size);
    }

    @PreAuthorize("hasRole('RECRUITER')")
    @PatchMapping("/applications/{applicationId}/status")
    public ApplicationResponse updateStatus(
            @PathVariable Long applicationId,
            @Valid @RequestBody UpdateApplicationStatusRequest request,
            Authentication auth
    ) {
        return applicationService.updateStatus(applicationId, auth.getName(), request.status());
    }

    @PreAuthorize("hasRole('CANDIDATE')")
    @GetMapping("/me/applications")
    public Page<MyApplicationResponse> myApplications (
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication auth
    ) {
        return applicationService.listMyApplications(auth.getName(), page, size);
    }
}
