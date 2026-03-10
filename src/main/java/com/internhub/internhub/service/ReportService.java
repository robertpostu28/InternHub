package com.internhub.internhub.service;

import com.internhub.internhub.api.dto.*;
import com.internhub.internhub.common.exception.ForbiddenException;
import com.internhub.internhub.common.exception.NotFoundException;
import com.internhub.internhub.domain.Job;
import com.internhub.internhub.domain.enums.JobStatus;
import com.internhub.internhub.repository.ApplicationRepository;
import com.internhub.internhub.repository.JobRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportService {
    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;

    public ReportService(JobRepository jobRepository, ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
    }

    // Provides an overview of a recruiter's jobs and applications
    // Ex: total jobs posted, number of open vs closed jobs, total applications received, breakdown of applications by status, etc.
    public RecruiterOverviewDto recruiterOverview(String recruiterEmail) {
        long totalJobs = jobRepository.countByRecruiterEmail(recruiterEmail);
        long openJobs = jobRepository.countByRecruiterEmailAndStatus(recruiterEmail, JobStatus.OPEN);
        long closedJobs = jobRepository.countByRecruiterEmailAndStatus(recruiterEmail, JobStatus.CLOSED);

        long totalApplications = applicationRepository.countAllForRecruiter(recruiterEmail);

        List<StatusCountDto> byStatus = applicationRepository.countByStatusForRecruiter(recruiterEmail).stream()
                .map(p -> new StatusCountDto(p.getStatus().name(), p.getCount()))
                .toList();

        return new RecruiterOverviewDto(totalJobs, openJobs, closedJobs, totalApplications, byStatus);
    }

    // Provides a list of top jobs based on the number of applications received
    // Ex: "Software Engineer Intern" - 50 applications, "Data Analyst Intern" - 30 applications, etc.
    public List<TopJobDto> topJobs(String recruiterEmail, int limit) {
        return applicationRepository.topJobsByApplications(recruiterEmail, PageRequest.of(0, limit)).stream()
                .map(p -> new TopJobDto(p.getJobId(), p.getTitle(), p.getStatus().name(), p.getApplicationsCount()))
                .toList();
    }

    // Provides a breakdown of application statuses for a specific job
    // Ex: for job "Software Engineer Intern", we might have 10 APPLIED, 5 REVIEWED, 2 INTERVIEW_SCHEDULED, etc.
    public JobStatusBreakdownDto jobStatusBreakdown(String recruiterEmail, Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new NotFoundException("JOB_NOT_FOUND", "Job not found with id: " + jobId));

        // recruiter can only view breakdown for their own job
        if (!job.getRecruiter().getEmail().equals(recruiterEmail)) {
            throw new ForbiddenException("ACCESS_DENIED", "You do not have permission to view this job's application breakdown");
        }

        List<StatusCountDto> counts = applicationRepository.countByStatusForJob(jobId).stream()
                .map(p -> new StatusCountDto(p.getStatus().name(), p.getCount()))
                .toList();

        return new JobStatusBreakdownDto(jobId, job.getTitle(), job.getStatus().name(), counts);
    }
}
