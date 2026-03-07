package com.internhub.internhub.service;

import com.internhub.internhub.domain.StoredFile;
import com.internhub.internhub.domain.User;
import com.internhub.internhub.domain.enums.FileType;
import com.internhub.internhub.domain.enums.Role;
import com.internhub.internhub.api.dto.CvMetadataDto;
import com.internhub.internhub.storage.CvStorageService;
import com.internhub.internhub.storage.DownloadedBlob;
import com.internhub.internhub.storage.StoredBlob;
import com.internhub.internhub.repository.StoredFileRepository;
import com.internhub.internhub.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Set;

@Service
public class CvService {
    private static final long MAX_CV_BYTES = 5 * 1024 * 1024; // 5 MB MVP limit
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    private final UserRepository userRepository;
    private final StoredFileRepository storedFileRepository;
    private final CvStorageService cvStorageService;

    public CvService(UserRepository userRepository, StoredFileRepository storedFileRepository, CvStorageService cvStorageService) {
        this.userRepository = userRepository;
        this.storedFileRepository = storedFileRepository;
        this.cvStorageService = cvStorageService;
    }

    @Transactional
    public CvMetadataDto uploadMyCv(String userEmail, MultipartFile file) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

        if (user.getRole() != Role.CANDIDATE) {
            throw new RuntimeException("Only candidates can upload CVs");
        }

        validate(file);

        // if user already has a CV: unlink -> delete blob -> delete DB row
        StoredFile old = user.getCvFile();
        if (old != null) {
            user.setCvFile(null);
            userRepository.save(user); // unlink first to avoid FK constraint

            cvStorageService.deleteCv(old.getStorageKey()); // getStorageKey is the blobName in storage service
            storedFileRepository.delete(old);
        }

        StoredBlob stored = cvStorageService.uploadCv(user.getId(), file);

        StoredFile sf = new StoredFile();
        sf.setOwner(user);
        sf.setFileType(FileType.CV);
        sf.setFileName(file.getOriginalFilename() == null ? "cv" : file.getOriginalFilename());
        sf.setContentType(stored.contentType());
        sf.setFileSize(stored.sizeBytes());
        sf.setStorageKey(stored.blobName()); // storageKey == blobName
        sf.setCreatedAt(LocalDateTime.now());

        StoredFile saved = storedFileRepository.save(sf);

        user.setCvFile(saved);
        userRepository.save(user);

        return new CvMetadataDto(
                saved.getId(),
                saved.getFileName(),
                saved.getContentType(),
                saved.getFileSize(),
                saved.getCreatedAt()
        );
    }

    public CvMetadataDto getMyCvMetadata(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        StoredFile cv = user.getCvFile();
        if (cv == null) {
            throw new RuntimeException("No CV uploaded");
        }

        return new CvMetadataDto(
                cv.getId(),
                cv.getFileName(),
                cv.getContentType(),
                cv.getFileSize(),
                cv.getCreatedAt()
        );
    }

    public DownloadedBlob downloadMyCv(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        StoredFile cv = user.getCvFile();
        if (cv == null) {
            throw new RuntimeException("No CV uploaded");
        }

        return cvStorageService.downloadCv(cv.getStorageKey());
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("CV file is required");
        }
        if (file.getSize() > MAX_CV_BYTES) {
            throw new RuntimeException("CV file size exceeds maximum of 5 MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new RuntimeException("CV file type must be PDF/DOC/DOCX");
        }
    }
}
