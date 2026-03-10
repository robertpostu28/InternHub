# 🏢 InternHub

A backend internship hiring platform built with **Java 21 + Spring Boot 4**. InternHub models a realistic end-to-end hiring workflow: recruiters post jobs and review applicants, candidates upload CVs and track their applications.

---

## 📋 Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Data Model](#data-model)
- [Features](#features)
- [API Endpoints](#api-endpoints)
- [Security & Authorization](#security--authorization)
- [Database & Migrations](#database--migrations)
- [File Storage](#file-storage)
- [Reporting](#reporting)
- [Testing](#testing)
- [Running Locally](#running-locally)

---

## 🔍 Overview

InternHub is a backend MVP for a recruiting platform. The core business flow:

1. A recruiter registers and creates job postings
2. A candidate registers and uploads their CV
3. The candidate browses open jobs and applies
4. The recruiter reviews applications, updates statuses, and downloads CVs
5. The recruiter uses reporting dashboards to track activity

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4 |
| Web | Spring Web MVC |
| Persistence | Spring Data JPA + PostgreSQL |
| Schema migrations | Flyway |
| Security | Spring Security (HTTP Basic + BCrypt) |
| File storage | Azure Blob Storage SDK / Azurite (local) |
| Build | Maven |
| Testing | Spring Boot Test, MockMvc |

---

## 🏗️ Architecture

InternHub follows a classic layered Spring architecture:
```
Controller (REST API)
     ↓
Service (Business Logic)
     ↓
Repository (Spring Data JPA)
     ↓
PostgreSQL + Azure Blob Storage
```

- **Controllers** — REST endpoints for auth, jobs, applications, CVs, and reports
- **Services** — business rules: ownership checks, status transitions, file handling
- **Repositories** — JPA persistence + report-style JPQL queries
- **Blob storage** — CV file content stored in Azurite; metadata stored in PostgreSQL

---

## 📐 Data Model

### User
Represents a platform account. Fields: `id`, `email`, `fullName`, `role` (`CANDIDATE` | `RECRUITER`), `passwordHash`, `createdAt`, `currentCvReference`.

### Job
A job posting owned by a recruiter. Fields: `id`, `title`, `description`, `requirements`, `location`, `status` (`OPEN` | `CLOSED`), `recruiter`, `createdAt`.

### Application
A candidate's application to a job. Fields: `id`, `job`, `candidate`, `status` (`APPLIED` | `SHORTLISTED` | ...), `appliedAt`.

### StoredFile
Metadata for uploaded files (CVs). Fields: `id`, `owner`, `type`, `originalFilename`, `contentType`, `size`, `storageKey`, `createdAt`.

**Database integrity enforced at the schema level:**
- Unique constraint on user email
- One application per candidate per job
- Foreign keys on all relationships
- Status constraints via enums

---

## ✨ Features

### Candidate
- Register and authenticate
- Upload, replace, and download their own CV
- Browse open jobs
- Apply to a job (requires a CV on file)
- Track application statuses

### Recruiter
- Register and authenticate
- Create and close job postings (own jobs only)
- View applications for their own jobs
- Update application statuses
- Download a candidate's CV (only for applications on their own jobs)
- Access reporting dashboards

### 🔒 Business Rules Enforced
- Candidates cannot apply without an uploaded CV
- Candidates cannot apply to the same job twice
- Recruiters can only manage their own jobs and applications
- Jobs must be open to accept applications
- Ownership is verified at the service layer for every sensitive operation

---

## 🌐 API Endpoints

### Auth
| Method | Path | Description |
|---|---|---|
| POST | `/auth/register` | Register a new user (candidate or recruiter) |

### Jobs
| Method | Path | Role | Description |
|---|---|---|---|
| POST | `/jobs` | RECRUITER | Create a job posting |
| GET | `/jobs` | Any | Browse all open jobs |
| GET | `/jobs/{id}` | Any | Get job details |
| PATCH | `/jobs/{id}/close` | RECRUITER | Close own job |

### Applications
| Method | Path | Role | Description |
|---|---|---|---|
| POST | `/jobs/{id}/applications` | CANDIDATE | Apply to a job |
| GET | `/jobs/{id}/applications` | RECRUITER | List applications for own job |
| PATCH | `/applications/{id}/status` | RECRUITER | Update application status |
| GET | `/me/applications` | CANDIDATE | View own applications |

### CV — Candidate
| Method | Path | Description |
|---|---|---|
| POST | `/me/cv` | Upload or replace CV |
| GET | `/me/cv` | Get own CV metadata |
| GET | `/me/cv/download` | Download own CV |

### CV — Recruiter
| Method | Path | Description |
|---|---|---|
| GET | `/applications/{id}/cv/download` | Download candidate CV (own jobs only) |

### Reports
| Method | Path | Description |
|---|---|---|
| GET | `/reports/overview` | Jobs and application summary |
| GET | `/reports/top-jobs` | Jobs ranked by application count |
| GET | `/reports/jobs/{id}/status-breakdown` | Application status breakdown for a job |

---

## 🔐 Security & Authorization

**Authentication:** HTTP Basic Auth. Every protected request includes credentials — no session state required. Suitable for an API-first backend MVP.

**Passwords:** Hashed with BCrypt before persistence.

**Authorization operates on two levels:**

- **Role-based** — only recruiters can create jobs; only recruiters can access report endpoints
- **Ownership-based** — recruiters can only close, view, or report on their own jobs; recruiters can only download CVs for applications belonging to their own jobs

---

## 🗄️ Database & Migrations

Schema is managed entirely through **Flyway** — no JPA auto-DDL. This means:

- Schema changes are versioned and reproducible
- Database setup is never manual
- Migrations are the single source of truth

---

## 📁 File Storage

CV files are handled with a clean separation of concerns:

- **File content** → Azure Blob Storage (Azurite locally)
- **File metadata** → PostgreSQL (`StoredFile` entity)

This means the relational model stays clean while binary content is stored efficiently. Recruiters can only access a candidate's CV if the candidate has an active application on one of the recruiter's jobs.

---

## 📊 Reporting

Recruiters have access to three analytics endpoints:

- **Overview** — total jobs (open/closed), total applications, applications by status
- **Top Jobs** — jobs ranked by number of applications received
- **Status Breakdown** — per-job breakdown of application statuses

These are backed by JPQL aggregation queries and provide genuine analytical value beyond transactional CRUD.

---

## 🧪 Testing

InternHub uses **Spring Boot integration tests** with `@SpringBootTest`, `@AutoConfigureMockMvc`, and `MockMvc`. Tests exercise the full Spring context including security, controllers, services, and the database.

**Coverage includes:**

- Unauthenticated access returns 401
- Role-based forbidden access (403)
- Successful registration and duplicate email rejection
- Invalid payload rejection and validation errors
- Recruiter job creation, closure, and ownership enforcement
- Candidate application flow (with and without CV)
- Duplicate application prevention
- Application status updates and ownership checks
- CV upload, metadata retrieval, and download (candidate and recruiter)
- Report endpoints: structure, data, forbidden access, missing resources

---

## 🚀 Running Locally

### Prerequisites
- Java 21
- Docker (for PostgreSQL and Azurite)
- Maven (or use the included wrapper)

### 1. Start infrastructure
```bash
docker-compose up -d
```

This starts:
- PostgreSQL on port `5432`
- Azurite (Azure Blob Storage emulator) on port `10000`

### 2. Run the application
```bash
./mvnw spring-boot:run
```

### 3. Use the API

The API is available at `http://localhost:8080`. Use curl, Postman, or any HTTP client with Basic Auth credentials.

**Example — register a recruiter:**
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email": "recruiter@example.com", "password": "secret123", "fullName": "Jane Smith", "role": "RECRUITER"}'
```

**Example — create a job (authenticated):**
```bash
curl -X POST http://localhost:8080/jobs \
  -u recruiter@example.com:secret123 \
  -H "Content-Type: application/json" \
  -d '{"title": "Backend Intern", "description": "...", "requirements": "...", "location": "Remote"}'
```

---

## ⚠️ Error Handling

All API errors return structured JSON:
```json
{
  "code": "CONFLICT",
  "message": "Candidate has already applied to this job.",
  "timestamp": "2025-06-01T10:00:00Z",
  "path": "/jobs/3/applications"
}
```

Validation errors additionally include field-level details.

---

## 🔮 Future Work

- Swagger / OpenAPI documentation
- JWT authentication (replacing Basic Auth)
- CI pipeline with GitHub Actions
- Testcontainers for better test isolation
- Pagination and filtering on job/application listings
- Frontend client
- Cloud deployment
