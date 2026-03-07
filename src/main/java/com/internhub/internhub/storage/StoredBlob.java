package com.internhub.internhub.storage;

/*
    When we upload a blob, we want to return some metadata about it. This record is a simple DTO for that purpose.
    DTO stands for Data Transfer Object, and it's a common pattern in Java to create simple classes (or records) that just
    hold data. In this case, we want to return the container name, blob name, size in bytes, and content type of the uploaded
    blob. This makes it easy for clients to understand what was uploaded and how to access it.
*/

public record StoredBlob (
        String container,
        String blobName,
        Long sizeBytes,
        String contentType
) {}
