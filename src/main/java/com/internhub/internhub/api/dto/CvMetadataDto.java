package com.internhub.internhub.api.dto;

import java.time.LocalDateTime;

public record CvMetadataDto (
        Long fileId,
        String fileName,
        String contentType,
        Long sizeBytes,
        LocalDateTime uploadedAt
) {}
