package com.internhub.internhub.repository;

import com.internhub.internhub.domain.StoredFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoredFileRepository extends JpaRepository<StoredFile, Long> {  }