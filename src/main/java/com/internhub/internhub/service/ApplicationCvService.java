package com.internhub.internhub.service;

import com.internhub.internhub.common.exception.ForbiddenException;
import com.internhub.internhub.common.exception.NotFoundException;
import com.internhub.internhub.domain.Application;
import com.internhub.internhub.domain.StoredFile;
import com.internhub.internhub.repository.ApplicationRepository;
import com.internhub.internhub.storage.CvStorageService;
import com.internhub.internhub.storage.DownloadedBlob;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service // This annotation indicates that this class is a service component in the Spring framework.
// It is a specialization of @Component and is used to indicate that the class provides business logic.
public class ApplicationCvService {
    public record CvDownload(Resource resource, Long sizeBytes, String contentType, String filename) {}

    private final ApplicationRepository applicationRepository;
    private final CvStorageService cvStorageService;

    public ApplicationCvService(ApplicationRepository applicationRepository, CvStorageService cvStorageService) {
        this.applicationRepository = applicationRepository;
        this.cvStorageService = cvStorageService;
    }

    public CvDownload downloadCvForApplication(Long applicationId, String recruiterEmail) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("APPLICATION_NOT_FOUND", "Application not found with id: " + applicationId));

        // Security check: ensure the recruiter is authorized to view this application
        if (!app.getJob().getRecruiter().getEmail().equals(recruiterEmail)) {
            throw new ForbiddenException("ACCESS_DENIED", "You are not authorized to access the CV for this application");
        }

        StoredFile cvFile = app.getCandidate().getCvFile();
        if (cvFile == null) {
            throw new NotFoundException("CV_NOT_FOUND", "The candidate has not uploaded a CV");
        }

        DownloadedBlob blob = cvStorageService.downloadCv(cvFile.getStorageKey());

        return new CvDownload(
                blob.resource(),
                blob.sizeBytes(),
                blob.contentType(),
                cvFile.getFileName()
        );
    }
}
