-- adds the domain tables: files, jobs, applications + constraints + indexes

-- files (CV / cover letter)
CREATE TABLE files (
    id BIGSERIAL PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(255) NOT NULL,
    size_bytes BIGINT NOT NULL,
    storage_key VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_files_user FOREIGN KEY (owner_id) REFERENCES users(id),
    CONSTRAINT check_file_type CHECK (type IN ('CV', 'COVER_LETTER'))
);

-- users: add optional CV link
ALTER TABLE users
    ADD COLUMN cv_file_id BIGINT;

ALTER TABLE users
    ADD CONSTRAINT fk_users_cv_file FOREIGN KEY (cv_file_id) REFERENCES files(id);

-- jobs posted by recruiters
CREATE TABLE jobs (
    id BIGSERIAL PRIMARY KEY,
    recruiter_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    requirements TEXT,
    location VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_jobs_recruiter FOREIGN KEY (recruiter_id) REFERENCES users(id),
    CONSTRAINT check_job_status CHECK (status IN ('OPEN', 'CLOSED'))
);

-- applications by candidates to jobs
CREATE TABLE applications (
    id BIGSERIAL PRIMARY KEY,
    job_id BIGINT NOT NULL,
    candidate_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'APPLIED',
    applied_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_applications_job FOREIGN KEY (job_id) REFERENCES jobs(id),
    CONSTRAINT fk_applications_candidate FOREIGN KEY (candidate_id) REFERENCES users(id),

    -- Ensure a candidate can only apply once per job
    CONSTRAINT uq_candidate_job UNIQUE (job_id, candidate_id),

    CONSTRAINT check_application_status CHECK (status IN ('APPLIED', 'SHORTLISTED', 'REJECTED', 'INTERVIEW_SCHEDULED', 'OFFERED'))
);

-- Indexes for performance
CREATE INDEX idx_jobs_status_created_at ON jobs(status, created_at); -- For filtering jobs by status and sorting by creation date
CREATE INDEX idx_app_job_status ON applications(job_id, status); -- For filtering applications by job and status
CREATE INDEX idx_app_candidate ON applications(candidate_id); -- For candidates to quickly find their applications
CREATE INDEX idx_files_owner ON files(owner_id); -- For users to quickly find their uploaded files