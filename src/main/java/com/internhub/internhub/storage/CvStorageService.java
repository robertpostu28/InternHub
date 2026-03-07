package com.internhub.internhub.storage;

import org.springframework.web.multipart.MultipartFile;

/*
    CvStorageService is an interface that defines the contract for our CV storage service. It has three methods:

    1. uploadCv: This method takes a candidate ID and a MultipartFile (which represents the uploaded file) and returns a
                 StoredBlob containing metadata about the uploaded CV.

    2. downloadCv: This method takes a blob name (which is the identifier for the stored CV) and returns a DownloadedBlob
                   containing the resource (the CV file), its size in bytes, and its content type.

    3. deleteCv: This method takes a blob name and deletes the corresponding CV from storage.
*/

public interface CvStorageService {
    StoredBlob uploadCv(Long candidateId, MultipartFile file);
    DownloadedBlob downloadCv(String blobName);
    void deleteCv(String blobName);
}
