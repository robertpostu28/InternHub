package com.internhub.internhub.domain;

import com.internhub.internhub.domain.enums.JobStatus;
import jakarta.persistence.*; // importing all JPA annotations
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/*
    In JPA, the @Column annotation's columnDefinition attribute allows you to specify the exact SQL data type for a column
    in the database. This is particularly useful when you want to use a specific data type that may not be directly mapped
    by JPA's default type mapping.
*/

@Entity
@Table(name = "jobs")
@Getter
@Setter
@NoArgsConstructor
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // matches BIGSERIAL
    private Long id;

    // job.recruiter_id -> user.id (recruiter_id is the foreign key so the child)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recruiter_id", nullable = false)
    private User recruiter; // the recruiter who posted the job

    @Column(name = "title", nullable = false, length = 255)
    private String title; // job title

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description; // detailed job description

    // set columnDefinition to TEXT to allow for longer requirements than a standard VARCHAR
    // if TEXT was not specified, JPA might default to a VARCHAR(255) which would truncate longer requirements
    @Column(name = "requirements", columnDefinition = "TEXT")
    private String requirements; // job requirements

    @Column(name = "location", length = 255)
    private String location; // job location (e.g., "New York, NY")

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private JobStatus status; // OPEN or CLOSED

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt; // timestamp for when the job was created
}
