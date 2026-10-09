package com.guvi.careernestbackend.Controller;

import com.guvi.careernestbackend.DTO.ApplyRequestDTO;
import com.guvi.careernestbackend.DTO.StatusUpdateRequestDTO;
import com.guvi.careernestbackend.Model.Application;
import com.guvi.careernestbackend.Service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    // 1. Submit Application for a Job
    // POST /api/applications/apply/{jobId}
    @PostMapping("/apply/{jobId}")
    public ResponseEntity<Map<String, Object>> applyForJob(
            @PathVariable String jobId,
            @Valid @RequestBody ApplyRequestDTO applyRequestDTO) {
        Map<String, Object> response = this.applicationService.applyForJob(jobId, applyRequestDTO);
        return ResponseEntity.ok(response);
    }

    // 2. View "My Applications" (Job Seeker)
    // GET /api/applications/my-applications/{seekerId}
    @GetMapping("/my-applications/{seekerId}")
    public ResponseEntity<List<Application>> getApplicationsBySeeker(@PathVariable String seekerId) {
        List<Application> applications = this.applicationService.getApplicationsBySeeker(seekerId);
        return ResponseEntity.ok(applications);
    }

    // 3. View all applicants for a job (Employer)
    // GET /api/applications/job/{jobId}
    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<Application>> getApplicationsByJob(@PathVariable String jobId) {
        List<Application> applications = this.applicationService.getApplicationsByJob(jobId);
        return ResponseEntity.ok(applications);
    }

    // 4. Update Application Status (Employer) & Trigger SMS
    // PATCH /api/applications/{id}/status
    @PatchMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateApplicationStatus(
            @PathVariable String id,
            @Valid @RequestBody StatusUpdateRequestDTO statusUpdateRequestDTO) {
        Map<String, Object> response = this.applicationService.updateApplicationStatus(id, statusUpdateRequestDTO);
        return ResponseEntity.ok(response);
    }
}