package com.internhub.internhub.api;

import com.internhub.internhub.api.dto.CvMetadataDto;
import com.internhub.internhub.service.CvService;
import com.internhub.internhub.storage.DownloadedBlob;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/me/cv")
public class MeCvController {
    private final CvService cvService;

    public MeCvController(CvService cvService) {
        this.cvService = cvService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE) // multipart/form-data is required for file uploads
    @PreAuthorize("hasRole('CANDIDATE')")
    public CvMetadataDto upload(Authentication auth,
                                @RequestParam("file") MultipartFile file) {
        return cvService.uploadMyCv(auth.getName(), file);
    }

    @GetMapping
    @PreAuthorize("hasRole('CANDIDATE')")
    public CvMetadataDto metadata(Authentication auth) {
        return cvService.getMyCvMetadata(auth.getName());
    }

    @GetMapping("/download")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<?> download(Authentication auth) {
        DownloadedBlob blob = cvService.downloadMyCv(auth.getName());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"cv\"") // force download with generic name "cv"
                .contentType(MediaType.parseMediaType(blob.contentType()))
                .contentLength(blob.sizeBytes())
                .body(blob.resource());
    }
}
