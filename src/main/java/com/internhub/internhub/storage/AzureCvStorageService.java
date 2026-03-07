package com.internhub.internhub.storage;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobProperties;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
public class AzureCvStorageService implements CvStorageService {
    private final BlobContainerClient cvContainer;

    public AzureCvStorageService(BlobContainerClient cvContainer) {
        this.cvContainer = cvContainer;
    }

    @Override
    public StoredBlob uploadCv(Long candidateId, MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = extractExtension(originalFilename);
            String blobName = "candidate/" + candidateId + "/cv/" + UUID.randomUUID() + extension;

            BlobClient blob = cvContainer.getBlobClient(blobName);

            String contentType = file.getContentType();
            if (contentType == null || contentType.isEmpty()) {
                contentType = "application/octet-stream"; // default to binary stream if content type is not provided
            }

            // upload bytes (overwrite = true so retries are safe)
            try (InputStream in = file.getInputStream()) {
                blob.upload(in, file.getSize(), true);
            }

            // set content type so download returns correct headers
            blob.setHttpHeaders(new BlobHttpHeaders().setContentType(contentType));

            return new StoredBlob(
                    cvContainer.getBlobContainerName(),
                    blobName,
                    file.getSize(),
                    contentType
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload CV", e);
        }
    }

    @Override
    public DownloadedBlob downloadCv(String blobName) {
        try {
            BlobClient blob = cvContainer.getBlobClient(blobName);
            BlobProperties properties = blob.getProperties();

            InputStream in = blob.openInputStream();
            Resource resource = new InputStreamResource(in);

            String contentType = properties.getContentType();
            if (contentType == null || contentType.isBlank()) {
                contentType = "application/octet-stream";
            }

            return new DownloadedBlob(
                    resource,
                    properties.getBlobSize(),
                    contentType
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to download CV from blob storage", e);
        }
    }

    @Override
    public void deleteCv(String blobName) {
        if (blobName == null || blobName.isBlank()) return;

        try {
            BlobClient blob = cvContainer.getBlobClient(blobName);
            blob.deleteIfExists();
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete CV from blob storage", e);
        }
    }

    private String extractExtension(String originalFilename) {
        if (originalFilename == null) return "";
        int dot = originalFilename.lastIndexOf('.');
        if (dot == -1) return "";
        return originalFilename.substring(dot); // includes the dot, e.g. ".pdf"
    }
}
