package com.internhub.internhub.service;

import com.internhub.internhub.api.dto.JobCreateRequest;
import com.internhub.internhub.api.dto.JobResponse;
import com.internhub.internhub.domain.Job;
import com.internhub.internhub.domain.User;
import com.internhub.internhub.domain.enums.JobStatus;
import com.internhub.internhub.repository.JobRepository;
import com.internhub.internhub.repository.UserRepository;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.internhub.internhub.api.dto.JobDetailsResponse;

@Service
public class JobService {
    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    public JobService(JobRepository jobRepository, UserRepository userRepository) {
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
    }

    public JobResponse createJob(String recruiterEmail, JobCreateRequest req) {
        User recruiter = userRepository.findByEmail(recruiterEmail)
                .orElseThrow(() -> new RuntimeException("Recruiter not found: " + recruiterEmail));

        Job job = new Job();
        job.setRecruiter(recruiter);
        job.setTitle(req.title().trim());
        job.setDescription(req.description().trim());
        job.setRequirements(req.requirements());
        job.setLocation(req.location());
        job.setStatus(JobStatus.OPEN); // New jobs are OPEN by default

        Job savedJob = jobRepository.save(job);

        return new JobResponse(
                savedJob.getId(),
                savedJob.getTitle(),
                savedJob.getLocation(),
                savedJob.getStatus().name(),
                savedJob.getCreatedAt()
        );
    }

    public Page<JobResponse> listOpenJobs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        return jobRepository.findByStatus(JobStatus.OPEN, pageable)
                .map(job -> new JobResponse(
                        job.getId(),
                        job.getTitle(),
                        job.getLocation(),
                        job.getStatus().name(),
                        job.getCreatedAt()
                ));
    }

    public JobDetailsResponse getJobById(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + jobId));

        return new JobDetailsResponse(
                job.getId(),
                job.getTitle(),
                job.getDescription(),
                job.getRequirements(),
                job.getLocation(),
                job.getStatus().name(),
                job.getRecruiter().getId(),
                job.getCreatedAt()
        );
    }

    public Page<JobResponse> listRecruiterJobs(String recruiterEmail, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        return jobRepository.findByRecruiterEmail(recruiterEmail, pageable)
                .map(job -> new JobResponse(
                        job.getId(),
                        job.getTitle(),
                        job.getLocation(),
                        job.getStatus().name(),
                        job.getCreatedAt()
                ));
    }

    public JobResponse closeJob(Long jobId, String recruiterEmail) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + jobId));

        // Ensure that the authenticated recruiter is the owner of the job before allowing it to be closed
        if (!job.getRecruiter().getEmail().equals(recruiterEmail)) {
            throw new RuntimeException("Unauthorized: You can only close your own jobs.");
        }

        job.setStatus(JobStatus.CLOSED);
        Job updatedJob = jobRepository.save(job);

        return new JobResponse(
                updatedJob.getId(),
                updatedJob.getTitle(),
                updatedJob.getLocation(),
                updatedJob.getStatus().name(),
                updatedJob.getCreatedAt()
        );
    }
}
