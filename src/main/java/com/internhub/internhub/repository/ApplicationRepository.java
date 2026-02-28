package com.internhub.internhub.repository;

import com.internhub.internhub.domain.Application;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application,Long> {
    boolean existsByJobIdAndCandidateId(Long jobId, Long candidateId);
}
