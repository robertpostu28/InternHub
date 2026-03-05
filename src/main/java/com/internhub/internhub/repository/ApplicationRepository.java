package com.internhub.internhub.repository;

import com.internhub.internhub.domain.Application;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ApplicationRepository extends JpaRepository<Application,Long> {
    boolean existsByJobIdAndCandidateId(Long jobId, Long candidateId);
    Page<Application> findByJobId(Long jobId, Pageable pageable);
    Page<Application> findByCandidateEmail(String email, Pageable pageable);
}
