package com.guvi.careernestbackend.Service;

import com.guvi.careernestbackend.DTO.ApplyRequestDTO;
import com.guvi.careernestbackend.DTO.StatusUpdateRequestDTO;
import com.guvi.careernestbackend.Model.Application;

import java.util.List;
import java.util.Map;

public interface ApplicationService {

    Map<String, Object> applyForJob(String jobId, ApplyRequestDTO request);
    List<Application> getApplicationsBySeeker(String seekerId);
    List<Application> getApplicationsByJob(String jobId);

    // 4. Employer updates application status and triggers Twilio SMS
    Map<String, Object> updateApplicationStatus(String applicationId, StatusUpdateRequestDTO request);
}