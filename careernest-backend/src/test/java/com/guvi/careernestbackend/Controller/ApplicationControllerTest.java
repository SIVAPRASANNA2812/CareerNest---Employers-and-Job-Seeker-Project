package com.guvi.careernestbackend.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guvi.careernestbackend.DTO.ApplyRequestDTO;
import com.guvi.careernestbackend.DTO.StatusUpdateRequestDTO;
import com.guvi.careernestbackend.Model.Application;
import com.guvi.careernestbackend.Model.ApplicationStatus;
import com.guvi.careernestbackend.Service.ApplicationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest(properties = "spring.data.mongodb.auto-index-creation=false")
public class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ApplicationService applicationService;

    @Test
    @DisplayName("POST /api/applications/apply/{jobId} - Success returns HTTP 200")
    void testApplyForJob_Success() throws Exception {
        ApplyRequestDTO request = new ApplyRequestDTO();
        request.setSeekerId("seeker-123");
        request.setResumeUrl("https://example.com/resume.pdf");
        request.setCoverLetter("Excited about this opportunity.");

        Map<String, Object> serviceResponse = new HashMap<>();
        serviceResponse.put("success", true);
        serviceResponse.put("message", "Application submitted successfully");

        when(applicationService.applyForJob(eq("job-999"), any(ApplyRequestDTO.class)))
                .thenReturn(serviceResponse);

        mockMvc.perform(post("/api/applications/apply/job-999")
                        .with(user("seeker").roles("JOB_SEEKER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Application submitted successfully"));
    }

    @Test
    @DisplayName("POST /api/applications/apply/{jobId} - Validation Failure (blank seekerId) returns 400")
    void testApplyForJob_ValidationFailure_Returns400() throws Exception {
        ApplyRequestDTO invalidRequest = new ApplyRequestDTO();
        invalidRequest.setSeekerId("");
        invalidRequest.setResumeUrl("");

        mockMvc.perform(post("/api/applications/apply/job-999")
                        .with(user("seeker").roles("JOB_SEEKER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.seekerId").exists())
                .andExpect(jsonPath("$.validationErrors.resumeUrl").exists());
    }

    @Test
    @DisplayName("POST /api/applications/apply/{jobId} - Role EMPLOYER is Forbidden (403)")
    void testApplyForJob_WrongRole_Returns403() throws Exception {
        ApplyRequestDTO request = new ApplyRequestDTO();
        request.setSeekerId("emp-123");
        request.setResumeUrl("https://example.com/resume.pdf");

        mockMvc.perform(post("/api/applications/apply/job-999")
                        .with(user("emp").roles("EMPLOYER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/applications/my-applications/{seekerId} - Returns candidate applications")
    void testGetApplicationsBySeeker_Success() throws Exception {
        Application app = new Application();
        app.setId("app-01");
        app.setSeekerId("seeker-123");
        app.setStatus(ApplicationStatus.APPLIED);

        when(applicationService.getApplicationsBySeeker("seeker-123")).thenReturn(List.of(app));

        mockMvc.perform(get("/api/applications/my-applications/seeker-123")
                        .with(user("seeker").roles("JOB_SEEKER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("app-01"))
                .andExpect(jsonPath("$[0].status").value("APPLIED"));
    }

    @Test
    @DisplayName("PATCH /api/applications/{id}/status - Employer updates status and triggers 200")
    void testUpdateStatus_Success() throws Exception {
        StatusUpdateRequestDTO request = new StatusUpdateRequestDTO();
        request.setStatus(ApplicationStatus.ACCEPTED);

        Map<String, Object> serviceResponse = new HashMap<>();
        serviceResponse.put("success", true);
        serviceResponse.put("message", "Application status updated to ACCEPTED");

        when(applicationService.updateApplicationStatus(eq("app-01"), any(StatusUpdateRequestDTO.class)))
                .thenReturn(serviceResponse);

        mockMvc.perform(patch("/api/applications/app-01/status")
                        .with(user("emp").roles("EMPLOYER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}