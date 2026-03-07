package com.internhub.internhub.storage;

import org.springframework.core.io.Resource;

/*
    Resource is a Spring Framework abstraction that represents a resource (like a file, classpath resource, etc.) and provides
    methods to access its content. In this case, it will represent the content of the downloaded blob.
*/

public record DownloadedBlob (
        Resource resource,
        Long sizeBytes,
        String contentType
) {}
