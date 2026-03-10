package com.internhub.internhub;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasItem;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@SpringBootTest
@AutoConfigureMockMvc
class InternHubApplicationTests {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    private long applyToJobAs(String email, String password, long jobId) throws Exception {
        MvcResult result = mockMvc.perform(post("/jobs/%d/apply".formatted(jobId))
                        .header("Authorization", basicAuth(email, password)))
                .andReturn();

        int statusCode = result.getResponse().getStatus();
        assertTrue(statusCode >= 200 && statusCode < 300, "Expected 2xx when applying, got " + statusCode);

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asLong();
    }

    private void closeJobAs(String email, String password, long jobId) throws Exception {
        MvcResult result = mockMvc.perform(patch("/jobs/%d/close".formatted(jobId))
                        .header("Authorization", basicAuth(email, password)))
                .andReturn();

        int statusCode = result.getResponse().getStatus();
        assertTrue(statusCode >= 200 && statusCode < 300, "Expected 2xx when closing job, got " + statusCode);
    }

    private MvcResult updateApplicationStatusAs(String email, String password, long applicationId, String newStatus) throws Exception {
        String requestBody = """
            {
              "status": "%s"
            }
            """.formatted(newStatus);

        return mockMvc.perform(patch("/applications/%d/status".formatted(applicationId))
                        .header("Authorization", basicAuth(email, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andReturn();
    }

    private String basicAuth(String email, String password) {
        String credentials = email + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    private void registerUser(String email, String fullName, String role) throws Exception {
        String requestBody = """
            {
              "email": "%s",
              "password": "Password123!",
              "fullName": "%s",
              "role": "%s"
            }
            """.formatted(email, fullName, role);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());
    }

    private long createJobAs(String email, String password, String title) throws Exception {
        String requestBody = """
            {
              "title": "%s",
              "description": "A backend internship role",
              "requirements": "Java, SQL",
              "location": "Cluj-Napoca"
            }
            """.formatted(title);

        MvcResult result = mockMvc.perform(post("/jobs")
                        .header("Authorization", basicAuth(email, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andReturn();

        int statusCode = result.getResponse().getStatus();
        assertTrue(statusCode >= 200 && statusCode < 300, "Expected 2xx when creating job, got " + statusCode);

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asLong();
    }

    private void uploadCvAs(String email, String password) throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cv.pdf",
                "application/pdf",
                "fake pdf content".getBytes(StandardCharsets.UTF_8)
        );

        MvcResult result = mockMvc.perform(multipart("/me/cv")
                        .file(file)
                        .header("Authorization", basicAuth(email, password)))
                .andReturn();

        int statusCode = result.getResponse().getStatus();
        assertTrue(statusCode >= 200 && statusCode < 300, "Expected 2xx when uploading CV, got " + statusCode);
    }

    @Test
    void protectedEndpointWithoutCredentialsReturns401() throws Exception {
        mockMvc.perform(get("/reports/recruiter/overview"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Authentication is required to access this resource."))
                .andExpect(jsonPath("$.path").value("/reports/recruiter/overview"));
    }

    @Test
    void registerUserSuccessfully() throws Exception {
        String email = "integration-candidate-" + UUID.randomUUID() + "@test.com";

        String requestBody = """
                {
                  "email": "%s",
                  "password": "Password123!",
                  "fullName": "Integration Candidate",
                  "role": "CANDIDATE"
                }
                """.formatted(email);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.fullName").value("Integration Candidate"))
                .andExpect(jsonPath("$.role").value("CANDIDATE"));
    }

    @Test
    void registerDuplicateEmailReturns409() throws Exception {
        String email = "duplicate-candidate-" + UUID.randomUUID() + "@test.com";

        String requestBody = """
                {
                  "email": "%s",
                  "password": "Password123!",
                  "fullName": "Duplicate Candidate",
                  "role": "CANDIDATE"
                }
                """.formatted(email);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.message").value("Email already exists"))
                .andExpect(jsonPath("$.path").value("/auth/register"));
    }

    @Test
    void registerInvalidPayloadReturns400() throws Exception {
        String requestBody = """
            {
              "email": "",
              "password": "123",
              "fullName": "",
              "role": ""
            }
            """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.path").value("/auth/register"))
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(jsonPath("$.fieldErrors[*].field").value(hasItems("email", "password", "fullName", "role")));
    }

    @Test
    void registerInvalidRoleReturns409() throws Exception {
        String email = "invalid-role-" + UUID.randomUUID() + "@test.com";

        String requestBody = """
            {
              "email": "%s",
              "password": "Password123!",
              "fullName": "Invalid Role User",
              "role": "ADMIN"
            }
            """.formatted(email);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_ROLE"))
                .andExpect(jsonPath("$.path").value("/auth/register"));
    }

    @Test
    void candidateOnRecruiterOverviewReturns403() throws Exception {
        String email = "candidate-reports-" + UUID.randomUUID() + "@test.com";
        registerUser(email, "Candidate Reports", "CANDIDATE");

        mockMvc.perform(get("/reports/recruiter/overview")
                        .header("Authorization", basicAuth(email, "Password123!")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.path").value("/reports/recruiter/overview"));
    }

    @Test
    void recruiterCreatesJobSuccessfully() throws Exception {
        String recruiterEmail = "recruiter-create-job-" + UUID.randomUUID() + "@test.com";
        registerUser(recruiterEmail, "Recruiter Create Job", "RECRUITER");

        String title = "Backend Intern " + UUID.randomUUID();

        String requestBody = """
            {
              "title": "%s",
              "description": "A backend internship role",
              "requirements": "Java, SQL",
              "location": "Cluj-Napoca"
            }
            """.formatted(title);

        MvcResult result = mockMvc.perform(post("/jobs")
                        .header("Authorization", basicAuth(recruiterEmail, "Password123!"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andReturn();

        int statusCode = result.getResponse().getStatus();
        assertTrue(statusCode >= 200 && statusCode < 300, "Expected 2xx when creating job, got " + statusCode);

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertTrue(json.get("id").asLong() > 0);
        assertEquals(title, json.get("title").asText());
        assertEquals("Cluj-Napoca", json.get("location").asText());
        assertEquals("OPEN", json.get("status").asText());
    }

    @Test
    void candidateCannotCreateJobReturns403() throws Exception {
        String candidateEmail = "candidate-create-job-" + UUID.randomUUID() + "@test.com";
        registerUser(candidateEmail, "Candidate Create Job", "CANDIDATE");

        String requestBody = """
            {
              "title": "Forbidden Job",
              "description": "Should not work",
              "requirements": "Java",
              "location": "Cluj-Napoca"
            }
            """;

        mockMvc.perform(post("/jobs")
                        .header("Authorization", basicAuth(candidateEmail, "Password123!"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void createJobInvalidPayloadReturns400() throws Exception {
        String recruiterEmail = "recruiter-invalid-job-" + UUID.randomUUID() + "@test.com";
        registerUser(recruiterEmail, "Recruiter Invalid Job", "RECRUITER");

        String requestBody = """
            {
              "title": "",
              "description": "",
              "requirements": "",
              "location": ""
            }
            """;

        mockMvc.perform(post("/jobs")
                        .header("Authorization", basicAuth(recruiterEmail, "Password123!"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(jsonPath("$.fieldErrors[*].field").value(hasItems("title", "description")));
    }

    @Test
    void candidateWithoutCvCannotApplyReturns400() throws Exception {
        String recruiterEmail = "recruiter-no-cv-" + UUID.randomUUID() + "@test.com";
        String candidateEmail = "candidate-no-cv-" + UUID.randomUUID() + "@test.com";

        registerUser(recruiterEmail, "Recruiter No CV", "RECRUITER");
        registerUser(candidateEmail, "Candidate No CV", "CANDIDATE");

        long jobId = createJobAs(recruiterEmail, "Password123!", "No CV Job " + UUID.randomUUID());

        mockMvc.perform(post("/jobs/%d/apply".formatted(jobId))
                        .header("Authorization", basicAuth(candidateEmail, "Password123!")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CV_REQUIRED"));
    }

    @Test
    void candidateAppliesSuccessfullyAfterUploadingCv() throws Exception {
        String recruiterEmail = "recruiter-apply-" + UUID.randomUUID() + "@test.com";
        String candidateEmail = "candidate-apply-" + UUID.randomUUID() + "@test.com";

        registerUser(recruiterEmail, "Recruiter Apply", "RECRUITER");
        registerUser(candidateEmail, "Candidate Apply", "CANDIDATE");

        long jobId = createJobAs(recruiterEmail, "Password123!", "Apply Job " + UUID.randomUUID());
        uploadCvAs(candidateEmail, "Password123!");

        MvcResult result = mockMvc.perform(post("/jobs/%d/apply".formatted(jobId))
                        .header("Authorization", basicAuth(candidateEmail, "Password123!")))
                .andReturn();

        int statusCode = result.getResponse().getStatus();
        assertTrue(statusCode >= 200 && statusCode < 300, "Expected 2xx when applying, got " + statusCode);

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertTrue(json.get("id").asLong() > 0);
        assertEquals(jobId, json.get("jobId").asLong());
        assertEquals("APPLIED", json.get("status").asText());
    }

    @Test
    void candidateCannotApplyTwiceReturns409() throws Exception {
        String recruiterEmail = "recruiter-dup-apply-" + UUID.randomUUID() + "@test.com";
        String candidateEmail = "candidate-dup-apply-" + UUID.randomUUID() + "@test.com";

        registerUser(recruiterEmail, "Recruiter Dup Apply", "RECRUITER");
        registerUser(candidateEmail, "Candidate Dup Apply", "CANDIDATE");

        long jobId = createJobAs(recruiterEmail, "Password123!", "Dup Apply Job " + UUID.randomUUID());
        uploadCvAs(candidateEmail, "Password123!");

        mockMvc.perform(post("/jobs/%d/apply".formatted(jobId))
                        .header("Authorization", basicAuth(candidateEmail, "Password123!")))
                .andReturn();

        mockMvc.perform(post("/jobs/%d/apply".formatted(jobId))
                        .header("Authorization", basicAuth(candidateEmail, "Password123!")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ALREADY_APPLIED"))
                .andExpect(jsonPath("$.message").value("You have already applied to this job"));
    }

    @Test
    void recruiterCanViewApplicationsForOwnJob() throws Exception {
        String recruiterEmail = "recruiter-view-apps-" + UUID.randomUUID() + "@test.com";
        String candidateEmail = "candidate-view-apps-" + UUID.randomUUID() + "@test.com";

        registerUser(recruiterEmail, "Recruiter View Apps", "RECRUITER");
        registerUser(candidateEmail, "Candidate View Apps", "CANDIDATE");

        long jobId = createJobAs(recruiterEmail, "Password123!", "View Apps Job " + UUID.randomUUID());
        uploadCvAs(candidateEmail, "Password123!");

        mockMvc.perform(post("/jobs/%d/apply".formatted(jobId))
                        .header("Authorization", basicAuth(candidateEmail, "Password123!")))
                .andReturn();

        mockMvc.perform(get("/jobs/%d/applications".formatted(jobId))
                        .header("Authorization", basicAuth(recruiterEmail, "Password123!")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].candidateEmail").value(candidateEmail))
                .andExpect(jsonPath("$.content[0].status").value("APPLIED"));
    }

    @Test
    void recruiterReportsOverviewReturns200() throws Exception {
        String recruiterEmail = "recruiter-overview-" + UUID.randomUUID() + "@test.com";
        registerUser(recruiterEmail, "Recruiter Overview", "RECRUITER");

        createJobAs(recruiterEmail, "Password123!", "Overview Job " + UUID.randomUUID());

        mockMvc.perform(get("/reports/recruiter/overview")
                        .header("Authorization", basicAuth(recruiterEmail, "Password123!")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalJobs").value(1))
                .andExpect(jsonPath("$.openJobs").value(1))
                .andExpect(jsonPath("$.closedJobs").value(0))
                .andExpect(jsonPath("$.totalApplications").value(0))
                .andExpect(jsonPath("$.applicationsByStatus").isArray());
    }

    @Test
    void protectedEndpointWithWrongCredentialsReturns401() throws Exception {
        mockMvc.perform(get("/reports/recruiter/overview")
                        .header("Authorization", basicAuth("wrong@test.com", "WrongPassword123!")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void candidateWithoutUploadedCvGets400OnMetadata() throws Exception {
        String candidateEmail = "candidate-no-cv-meta-" + UUID.randomUUID() + "@test.com";
        registerUser(candidateEmail, "Candidate No CV Meta", "CANDIDATE");

        mockMvc.perform(get("/me/cv")
                        .header("Authorization", basicAuth(candidateEmail, "Password123!")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CV_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("No CV uploaded"))
                .andExpect(jsonPath("$.path").value("/me/cv"));
    }

    @Test
    void candidateWithoutUploadedCvGets400OnDownload() throws Exception {
        String candidateEmail = "candidate-no-cv-download-" + UUID.randomUUID() + "@test.com";
        registerUser(candidateEmail, "Candidate No CV Download", "CANDIDATE");

        mockMvc.perform(get("/me/cv/download")
                        .header("Authorization", basicAuth(candidateEmail, "Password123!")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CV_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("No CV uploaded"))
                .andExpect(jsonPath("$.path").value("/me/cv/download"));
    }

    @Test
    void candidateCanSeeOwnCvMetadata() throws Exception {
        String candidateEmail = "candidate-cv-meta-" + UUID.randomUUID() + "@test.com";
        registerUser(candidateEmail, "Candidate CV Meta", "CANDIDATE");
        uploadCvAs(candidateEmail, "Password123!");

        mockMvc.perform(get("/me/cv")
                        .header("Authorization", basicAuth(candidateEmail, "Password123!")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").value("cv.pdf"))
                .andExpect(jsonPath("$.contentType").value("application/pdf"))
                .andExpect(jsonPath("$.sizeBytes").value(16));
    }

    @Test
    void candidateCanDownloadOwnCv() throws Exception {
        String candidateEmail = "candidate-cv-download-" + UUID.randomUUID() + "@test.com";
        registerUser(candidateEmail, "Candidate CV Download", "CANDIDATE");
        uploadCvAs(candidateEmail, "Password123!");

        mockMvc.perform(get("/me/cv/download")
                        .header("Authorization", basicAuth(candidateEmail, "Password123!")))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("application/pdf")));
    }

    @Test
    void recruiterCanDownloadCandidateCvForOwnJob() throws Exception {
        String recruiterEmail = "recruiter-own-cv-download-" + UUID.randomUUID() + "@test.com";
        String candidateEmail = "candidate-own-cv-download-" + UUID.randomUUID() + "@test.com";

        registerUser(recruiterEmail, "Recruiter Own CV Download", "RECRUITER");
        registerUser(candidateEmail, "Candidate Own CV Download", "CANDIDATE");

        long jobId = createJobAs(recruiterEmail, "Password123!", "Own CV Download Job " + UUID.randomUUID());
        uploadCvAs(candidateEmail, "Password123!");
        long applicationId = applyToJobAs(candidateEmail, "Password123!", jobId);

        mockMvc.perform(get("/applications/%d/cv/download".formatted(applicationId))
                        .header("Authorization", basicAuth(recruiterEmail, "Password123!")))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("application/pdf")));
    }

    @Test
    void recruiterCannotDownloadCandidateCvForAnotherRecruitersJobReturns403() throws Exception {
        String recruiterA = "recruiter-a-cv-" + UUID.randomUUID() + "@test.com";
        String recruiterB = "recruiter-b-cv-" + UUID.randomUUID() + "@test.com";
        String candidateEmail = "candidate-forbidden-cv-" + UUID.randomUUID() + "@test.com";

        registerUser(recruiterA, "Recruiter A CV", "RECRUITER");
        registerUser(recruiterB, "Recruiter B CV", "RECRUITER");
        registerUser(candidateEmail, "Candidate Forbidden CV", "CANDIDATE");

        long jobId = createJobAs(recruiterA, "Password123!", "Forbidden CV Job " + UUID.randomUUID());
        uploadCvAs(candidateEmail, "Password123!");
        long applicationId = applyToJobAs(candidateEmail, "Password123!", jobId);

        mockMvc.perform(get("/applications/%d/cv/download".formatted(applicationId))
                        .header("Authorization", basicAuth(recruiterB, "Password123!")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    void recruiterGets404WhenDownloadingCvForMissingApplication() throws Exception {
        String recruiterEmail = "recruiter-missing-app-cv-" + UUID.randomUUID() + "@test.com";
        registerUser(recruiterEmail, "Recruiter Missing App CV", "RECRUITER");

        mockMvc.perform(get("/applications/999999/cv/download")
                        .header("Authorization", basicAuth(recruiterEmail, "Password123!")))
                .andExpect(status().isNotFound());
    }

    @Test
    void recruiterCanCloseOwnJobSuccessfully() throws Exception {
        String recruiterEmail = "recruiter-close-own-" + UUID.randomUUID() + "@test.com";
        registerUser(recruiterEmail, "Recruiter Close Own", "RECRUITER");

        long jobId = createJobAs(recruiterEmail, "Password123!", "Close Own Job " + UUID.randomUUID());
        closeJobAs(recruiterEmail, "Password123!", jobId);

        mockMvc.perform(get("/reports/recruiter/overview")
                        .header("Authorization", basicAuth(recruiterEmail, "Password123!")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalJobs").value(1))
                .andExpect(jsonPath("$.openJobs").value(0))
                .andExpect(jsonPath("$.closedJobs").value(1));
    }

    @Test
    void recruiterCannotCloseAnotherRecruitersJobReturns403() throws Exception {
        String recruiterA = "recruiter-a-close-" + UUID.randomUUID() + "@test.com";
        String recruiterB = "recruiter-b-close-" + UUID.randomUUID() + "@test.com";

        registerUser(recruiterA, "Recruiter A Close", "RECRUITER");
        registerUser(recruiterB, "Recruiter B Close", "RECRUITER");

        long jobId = createJobAs(recruiterA, "Password123!", "Forbidden Close Job " + UUID.randomUUID());

        mockMvc.perform(patch("/jobs/%d/close".formatted(jobId))
                        .header("Authorization", basicAuth(recruiterB, "Password123!")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    void closedJobDisappearsFromCandidateFeed() throws Exception {
        String recruiterEmail = "recruiter-public-feed-" + UUID.randomUUID() + "@test.com";
        String candidateEmail = "candidate-public-feed-" + UUID.randomUUID() + "@test.com";

        registerUser(recruiterEmail, "Recruiter Public Feed", "RECRUITER");
        registerUser(candidateEmail, "Candidate Public Feed", "CANDIDATE");

        String title = "Closed Feed Job " + UUID.randomUUID();
        long jobId = createJobAs(recruiterEmail, "Password123!", title);
        closeJobAs(recruiterEmail, "Password123!", jobId);

        mockMvc.perform(get("/jobs")
                        .header("Authorization", basicAuth(candidateEmail, "Password123!")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].title").value(not(hasItem(title))));
    }

    @Test
    void applyToClosedJobReturns409Or400() throws Exception {
        String recruiterEmail = "recruiter-closed-apply-" + UUID.randomUUID() + "@test.com";
        String candidateEmail = "candidate-closed-apply-" + UUID.randomUUID() + "@test.com";

        registerUser(recruiterEmail, "Recruiter Closed Apply", "RECRUITER");
        registerUser(candidateEmail, "Candidate Closed Apply", "CANDIDATE");

        long jobId = createJobAs(recruiterEmail, "Password123!", "Closed Apply Job " + UUID.randomUUID());
        closeJobAs(recruiterEmail, "Password123!", jobId);
        uploadCvAs(candidateEmail, "Password123!");

        MvcResult result = mockMvc.perform(post("/jobs/%d/apply".formatted(jobId))
                        .header("Authorization", basicAuth(candidateEmail, "Password123!")))
                .andReturn();

        int statusCode = result.getResponse().getStatus();
        assertTrue(statusCode == 400 || statusCode == 409, "Expected 400 or 409 when applying to closed job, got " + statusCode);
    }

    @Test
    void recruiterCanUpdateApplicationStatusSuccessfully() throws Exception {
        String recruiterEmail = "recruiter-update-status-" + UUID.randomUUID() + "@test.com";
        String candidateEmail = "candidate-update-status-" + UUID.randomUUID() + "@test.com";

        registerUser(recruiterEmail, "Recruiter Update Status", "RECRUITER");
        registerUser(candidateEmail, "Candidate Update Status", "CANDIDATE");

        long jobId = createJobAs(recruiterEmail, "Password123!", "Update Status Job " + UUID.randomUUID());
        uploadCvAs(candidateEmail, "Password123!");
        long applicationId = applyToJobAs(candidateEmail, "Password123!", jobId);

        MvcResult result = updateApplicationStatusAs(recruiterEmail, "Password123!", applicationId, "SHORTLISTED");
        int statusCode = result.getResponse().getStatus();
        assertTrue(statusCode >= 200 && statusCode < 300, "Expected 2xx when updating application status, got " + statusCode);

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertEquals("SHORTLISTED", json.get("status").asText());
    }

    @Test
    void recruiterCannotUpdateApplicationStatusForAnotherRecruitersJobReturns403() throws Exception {
        String recruiterA = "recruiter-a-update-status-" + UUID.randomUUID() + "@test.com";
        String recruiterB = "recruiter-b-update-status-" + UUID.randomUUID() + "@test.com";
        String candidateEmail = "cand-upd-forb" + UUID.randomUUID() + "@test.com";

        registerUser(recruiterA, "Recruiter A Update Status", "RECRUITER");
        registerUser(recruiterB, "Recruiter B Update Status", "RECRUITER");
        registerUser(candidateEmail, "Candidate Update Status Forbidden", "CANDIDATE");

        long jobId = createJobAs(recruiterA, "Password123!", "Forbidden Update Status Job " + UUID.randomUUID());
        uploadCvAs(candidateEmail, "Password123!");
        long applicationId = applyToJobAs(candidateEmail, "Password123!", jobId);

        String requestBody = """
            {
              "status": "SHORTLISTED"
            }
            """;

        mockMvc.perform(patch("/applications/%d/status".formatted(applicationId))
                        .header("Authorization", basicAuth(recruiterB, "Password123!"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    void updateApplicationStatusInvalidPayloadReturns400() throws Exception {
        String recruiterEmail = "recruiter-invalid-status-" + UUID.randomUUID() + "@test.com";
        String candidateEmail = "candidate-invalid-status-" + UUID.randomUUID() + "@test.com";

        registerUser(recruiterEmail, "Recruiter Invalid Status", "RECRUITER");
        registerUser(candidateEmail, "Candidate Invalid Status", "CANDIDATE");

        long jobId = createJobAs(recruiterEmail, "Password123!", "Invalid Status Job " + UUID.randomUUID());
        uploadCvAs(candidateEmail, "Password123!");
        long applicationId = applyToJobAs(candidateEmail, "Password123!", jobId);

        String requestBody = """
            {
              "status": "NOT_A_REAL_STATUS"
            }
            """;

        mockMvc.perform(patch("/applications/%d/status".formatted(applicationId))
                        .header("Authorization", basicAuth(recruiterEmail, "Password123!"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.path").value("/applications/%d/status".formatted(applicationId)));
    }

    @Test
    void recruiterReportsTopJobsReturns200() throws Exception {
        String recruiterEmail = "recruiter-top-jobs-" + UUID.randomUUID() + "@test.com";
        String candidateEmail = "candidate-top-jobs-" + UUID.randomUUID() + "@test.com";

        registerUser(recruiterEmail, "Recruiter Top Jobs", "RECRUITER");
        registerUser(candidateEmail, "Candidate Top Jobs", "CANDIDATE");

        String quietTitle = "Quiet Job " + UUID.randomUUID();
        String popularTitle = "Popular Job " + UUID.randomUUID();

        createJobAs(recruiterEmail, "Password123!", quietTitle);
        long popularJobId = createJobAs(recruiterEmail, "Password123!", popularTitle);

        uploadCvAs(candidateEmail, "Password123!");
        applyToJobAs(candidateEmail, "Password123!", popularJobId);

        mockMvc.perform(get("/reports/recruiter/top-jobs")
                        .param("limit", "5")
                        .header("Authorization", basicAuth(recruiterEmail, "Password123!")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].jobId").value((int) popularJobId))
                .andExpect(jsonPath("$[0].title").value(popularTitle));
    }

    @Test
    void recruiterJobStatusBreakdownReturns200() throws Exception {
        String recruiterEmail = "recruiter-breakdown-" + UUID.randomUUID() + "@test.com";
        String candidateEmail = "candidate-breakdown-" + UUID.randomUUID() + "@test.com";

        registerUser(recruiterEmail, "Recruiter Breakdown", "RECRUITER");
        registerUser(candidateEmail, "Candidate Breakdown", "CANDIDATE");

        long jobId = createJobAs(recruiterEmail, "Password123!", "Breakdown Job " + UUID.randomUUID());
        uploadCvAs(candidateEmail, "Password123!");
        applyToJobAs(candidateEmail, "Password123!", jobId);

        mockMvc.perform(get("/reports/jobs/%d/status-breakdown".formatted(jobId))
                        .header("Authorization", basicAuth(recruiterEmail, "Password123!")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId").value((int) jobId))
                .andExpect(content().string(containsString("APPLIED")));
    }

    @Test
    void recruiterJobStatusBreakdownForMissingJobReturns404() throws Exception {
        String recruiterEmail = "recruiter-breakdown-missing-" + UUID.randomUUID() + "@test.com";
        registerUser(recruiterEmail, "Recruiter Breakdown Missing", "RECRUITER");

        mockMvc.perform(get("/reports/jobs/999999/status-breakdown")
                        .header("Authorization", basicAuth(recruiterEmail, "Password123!")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("JOB_NOT_FOUND"));
    }

    @Test
    void recruiterJobStatusBreakdownForAnotherRecruitersJobReturns403() throws Exception {
        String recruiterA = "recruiter-a-breakdown-" + UUID.randomUUID() + "@test.com";
        String recruiterB = "recruiter-b-breakdown-" + UUID.randomUUID() + "@test.com";

        registerUser(recruiterA, "Recruiter A Breakdown", "RECRUITER");
        registerUser(recruiterB, "Recruiter B Breakdown", "RECRUITER");

        long jobId = createJobAs(recruiterA, "Password123!", "Forbidden Breakdown Job " + UUID.randomUUID());

        mockMvc.perform(get("/reports/jobs/%d/status-breakdown".formatted(jobId))
                        .header("Authorization", basicAuth(recruiterB, "Password123!")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    void recruiterOverviewCountsApplicationsAfterApply() throws Exception {
        String recruiterEmail = "recruiter-overview-counts-" + UUID.randomUUID() + "@test.com";
        String candidateEmail = "candidate-overview-counts-" + UUID.randomUUID() + "@test.com";

        registerUser(recruiterEmail, "Recruiter Overview Counts", "RECRUITER");
        registerUser(candidateEmail, "Candidate Overview Counts", "CANDIDATE");

        long jobId = createJobAs(recruiterEmail, "Password123!", "Overview Counts Job " + UUID.randomUUID());
        uploadCvAs(candidateEmail, "Password123!");
        applyToJobAs(candidateEmail, "Password123!", jobId);

        mockMvc.perform(get("/reports/recruiter/overview")
                        .header("Authorization", basicAuth(recruiterEmail, "Password123!")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalJobs").value(1))
                .andExpect(jsonPath("$.openJobs").value(1))
                .andExpect(jsonPath("$.closedJobs").value(0))
                .andExpect(jsonPath("$.totalApplications").value(1))
                .andExpect(content().string(containsString("APPLIED")));
    }
}