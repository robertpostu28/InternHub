package com.internhub.internhub.repository;

import com.internhub.internhub.domain.Application;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.internhub.internhub.domain.enums.ApplicationStatus;
import com.internhub.internhub.domain.enums.JobStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application,Long> {
    boolean existsByJobIdAndCandidateId(Long jobId, Long candidateId);
    Page<Application> findByJobId(Long jobId, Pageable pageable);
    Page<Application> findByCandidateEmail(String email, Pageable pageable);

    // Projections for status breakdown and top jobs
    // These interfaces are public by default and can be used as return types for the custom queries below
    interface StatusCountProjection {
        ApplicationStatus getStatus();
        long getCount();
    }

    interface TopJobProjection {
        Long getJobId();
        String getTitle();
        JobStatus getStatus();
        long getApplicationsCount();
    }

    /*
        These custom queries use JPQL to fetch aggregated data for reporting purposes.
        It's not SQL, but rather a query language that works with JPA entities.

        JPQL stands for Java Persistence Query Language, and it allows us to write queries that are independent of the underlying database.
    */

    // Report queries
    @Query("""
        select count(a)
        from Application a
        where a.job.recruiter.email = :email
    """)
    long countAllForRecruiter(@Param("email") String email); // Total applications for a recruiter

    @Query("""
        select a.status as status, count(a) as count
        from Application a
        where a.job.recruiter.email = :email
        group by a.status
    """)
    List<StatusCountProjection> countByStatusForRecruiter(@Param("email") String email); // Status breakdown for a recruiter

    @Query("""
        select a.status as status, count(a) as count
        from Application a
        where a.job.id = :jobId
        group by a.status
    """)
    List<StatusCountProjection> countByStatusForJob(@Param("jobId") Long jobId); // Status breakdown for a specific job

    @Query("""
        select j.id as jobId, j.title as title, j.status as status, count(a) as applicationsCount
        from Application a
        join a.job j
        where j.recruiter.email = :email
        group by j.id, j.title, j.status
        order by count(a) desc
    """)
    List<TopJobProjection> topJobsByApplications(@Param("email") String email, Pageable pageable); // Top jobs by application count for a recruiter
}
