package com.guvi.careernestbackend.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.guvi.careernestbackend.DTO.JobRequestDTO;
import com.guvi.careernestbackend.Model.Job;
import com.guvi.careernestbackend.Model.JobStatus;
import com.guvi.careernestbackend.Service.JobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest(properties = "spring.data.mongodb.auto-index-creation=false")
public class JobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private JobService jobService;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("GET /api/jobs/{id} - Public access returns job details")
    void testGetJobById_Public_Success() throws Exception {
        Job job = new Job();
        job.setId("job-101");
        job.setTitle("Senior Full Stack Developer");
        job.setCompanyName("Tech Solutions");
        job.setStatus(JobStatus.OPEN);

        when(jobService.getJobById("job-101")).thenReturn(job);

        mockMvc.perform(get("/api/jobs/job-101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("job-101"))
                .andExpect(jsonPath("$.title").value("Senior Full Stack Developer"));
    }

    @Test
    @DisplayName("GET /api/jobs/{id} - Not Found returns 404")
    void testGetJobById_NotFound_Returns404() throws Exception {
        when(jobService.getJobById("missing-job")).thenReturn(null);

        mockMvc.perform(get("/api/jobs/missing-job"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/jobs - Employer creates job returns 200 OK")
    void testCreateJob_Employer_Success() throws Exception {
        JobRequestDTO request = new JobRequestDTO();
        request.setTitle("DevOps Architect");
        request.setDescription("Expertise in Docker, Kubernetes, AWS");
        request.setCompanyName("CloudOps");
        request.setLocation("Chennai");
        request.setSalary("30 LPA");
        request.setDeadline(LocalDate.now().plusMonths(1));
        request.setEmployerId("emp-888");

        Map<String, Object> serviceResponse = new HashMap<>();
        serviceResponse.put("success", true);
        serviceResponse.put("message", "Job posted successfully");

        when(jobService.createJob(any(JobRequestDTO.class))).thenReturn(serviceResponse);

        mockMvc.perform(post("/api/jobs")
                        .with(user("emp").roles("EMPLOYER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Job posted successfully"));
    }

    @Test
    @DisplayName("POST /api/jobs - Validation failure (blank fields) returns 400 Bad Request")
    void testCreateJob_ValidationFailure_Returns400() throws Exception {
        JobRequestDTO invalidRequest = new JobRequestDTO();
        invalidRequest.setTitle("");
        invalidRequest.setDescription("");

        mockMvc.perform(post("/api/jobs")
                        .with(user("emp").roles("EMPLOYER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.title").exists());
    }

    @Test
    @DisplayName("POST /api/jobs - Role JOB_SEEKER is Forbidden (403)")
    void testCreateJob_JobSeeker_Forbidden403() throws Exception {
        JobRequestDTO request = new JobRequestDTO();
        request.setTitle("Unauthorized Job");

        mockMvc.perform(post("/api/jobs")
                        .with(user("seeker").roles("JOB_SEEKER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/jobs/{id} - Employer deletes job returns 200 OK")
    void testDeleteJob_Success() throws Exception {
        Map<String, Object> serviceResponse = new HashMap<>();
        serviceResponse.put("success", true);
        serviceResponse.put("message", "Job deleted successfully");

        when(jobService.deleteJob("job-101")).thenReturn(serviceResponse);

        mockMvc.perform(delete("/api/jobs/job-101")
                        .with(user("emp").roles("EMPLOYER"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/jobs/employer/{employerId} - Returns jobs posted by employer")
    void testGetJobsByEmployer_Success() throws Exception {
        Job job = new Job();
        job.setId("job-55");
        job.setEmployerId("emp-888");

        when(jobService.getJobsByEmployer("emp-888")).thenReturn(List.of(job));

        mockMvc.perform(get("/api/jobs/employer/emp-888"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("job-55"));
    }
}