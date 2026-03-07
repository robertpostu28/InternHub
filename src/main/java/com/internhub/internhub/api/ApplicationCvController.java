package com.internhub.internhub.api;

import com.internhub.internhub.service.ApplicationCvService;
import com.internhub.internhub.service.ApplicationCvService.CvDownload;
import com.internhub.internhub.service.ApplicationService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/*
    This controller provides an endpoint for recruiters to download the CV associated with a specific application.
    It ensures that only users with the RECRUITER role can access this functionality and that they can only download
    CVs for applications they are authorized to view.
*/

@RestController
@RequestMapping("/applications")
public class ApplicationCvController {
    private final ApplicationCvService applicationCvService;

    public ApplicationCvController(ApplicationCvService applicationCvService) {
        this.applicationCvService = applicationCvService;
    }

    @GetMapping("/{applicationId}/cv/download")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<Resource> downloadCv(@PathVariable Long applicationId, Authentication auth) {
        CvDownload result = applicationCvService.downloadCvForApplication(applicationId, auth.getName());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + result.filename().replace("\"", "") + "\"")
                .contentType(MediaType.parseMediaType(result.contentType()))
                .contentLength(result.sizeBytes())
                .body(result.resource());
    }
}
