package com.internhub.internhub.service;

import com.internhub.internhub.api.dto.ApplicationResponse;
import com.internhub.internhub.common.exception.BadRequestException;
import com.internhub.internhub.common.exception.ConflictException;
import com.internhub.internhub.common.exception.ForbiddenException;
import com.internhub.internhub.common.exception.NotFoundException;
import com.internhub.internhub.domain.Application;
import com.internhub.internhub.domain.Job;
import com.internhub.internhub.domain.User;
import com.internhub.internhub.domain.enums.ApplicationStatus;
import com.internhub.internhub.domain.enums.JobStatus;
import com.internhub.internhub.repository.ApplicationRepository;
import com.internhub.internhub.repository.JobRepository;
import com.internhub.internhub.repository.UserRepository;
import org.springframework.stereotype.Service;

import com.internhub.internhub.api.dto.JobApplicationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.internhub.internhub.api.dto.MyApplicationResponse;

@Service
public class ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private  final UserRepository userRepository;

    public ApplicationService(ApplicationRepository applicationRepository, JobRepository jobRepository, UserRepository userRepository) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
    }

    public ApplicationResponse apply(String candidateEmail, Long jobId) {
        User candidate = userRepository.findByEmail(candidateEmail)
                .orElseThrow(() -> new NotFoundException("CANDIDATE_NOT_FOUND", "Candidate not found with email: " + candidateEmail));

        // Rule: candidate must have cv uploaded to apply
        if (candidate.getCvFile() == null) {
            throw new BadRequestException("CV_REQUIRED", "You must upload a CV before applying to jobs");
        }

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new NotFoundException("JOB_NOT_FOUND", "Job not found with id: " + jobId));

        // Rule: job must be OPEN to accept applications
        if (job.getStatus() != JobStatus.OPEN) {
            throw new ConflictException("JOB_STATUS_NOT_OPEN", "Job status is not OPEN");
        }

        // Rule: candidate cannot apply to the same job more than once
        if (applicationRepository.existsByJobIdAndCandidateId(jobId, candidate.getId())) {
            throw new ConflictException("ALREADY_APPLIED", "You have already applied to this job");
        }

        Application app = new Application();
        app.setCandidate(candidate);
        app.setJob(job);
        app.setStatus(ApplicationStatus.APPLIED); // New applications start with APPLIED status

        Application savedApp = applicationRepository.save(app);

        return new ApplicationResponse(
                savedApp.getId(),
                savedApp.getJob().getId(),
                savedApp.getCandidate().getId(),
                savedApp.getStatus().name(),
                savedApp.getAppliedAt()
        );
    }

    public Page<JobApplicationResponse> listApplicationsForJob(Long jobId, String recruiterEmail, int page, int size) {
        Job job =  jobRepository.findById(jobId)
                .orElseThrow(() -> new NotFoundException("JOB_NOT_FOUND", "Job not found with id: " + jobId));

        // Rule: recruiter can only see applications for their own job
        if (!job.getRecruiter().getEmail().equals(recruiterEmail)) {
            throw new ForbiddenException("ACCESS_DENIED", "You are not allowed to view applications for this job");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "appliedAt"));

        return applicationRepository.findByJobId(jobId, pageable)
                .map(application -> new JobApplicationResponse(
                        application.getId(),
                        application.getCandidate().getId(),
                        application.getCandidate().getEmail(),
                        application.getCandidate().getFullName(),
                        application.getStatus().name(),
                        application.getAppliedAt()
                ));
    }

    public ApplicationResponse updateStatus(Long applicationId, String recruiterEmail, String newStatus) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("APPLICATION_NOT_FOUND", "Application not found with id: " + applicationId));

        // Rule: recruiter can only update applications for their own job
        if (!application.getJob().getRecruiter().getEmail().equals(recruiterEmail)) {
            throw new ForbiddenException("ACCESS_DENIED", "You are not allowed to update applications for this job");
        }

        ApplicationStatus status;
        try {
            status = ApplicationStatus.valueOf(newStatus.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("INVALID_STATUS", "Invalid application status: " + newStatus);
        }

        application.setStatus(status);

        Application saved =  applicationRepository.save(application);

        return new ApplicationResponse(
                saved.getId(),
                saved.getJob().getId(),
                saved.getCandidate().getId(),
                saved.getStatus().name(),
                saved.getAppliedAt()
        );
    }

    public Page<MyApplicationResponse> listMyApplications(String candidateEmail, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "appliedAt"));

        return applicationRepository.findByCandidateEmail(candidateEmail, pageable)
                .map(application -> new MyApplicationResponse(
                        application.getId(),
                        application.getJob().getId(),
                        application.getJob().getTitle(),
                        application.getJob().getLocation(),
                        application.getStatus().name(),
                        application.getAppliedAt()
                ));

    }
}
