package com.internhub.internhub.domain;

import com.internhub.internhub.domain.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "applications",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_candidate_job",
                columnNames = {"job_id", "candidate_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // matches BIGSERIAL
    private Long id;

    // application.job_id -> job.id (job_id is the foreign key so the child)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job; // the job being applied to

    // application.candidate_id -> user.id (candidate_id is the foreign key so the child)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    private User candidate; // the candidate applying for the job

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private ApplicationStatus status;

    @Column(name = "applied_at", nullable = false)
    private LocalDateTime createdAt; // timestamp for when the application was created
}
