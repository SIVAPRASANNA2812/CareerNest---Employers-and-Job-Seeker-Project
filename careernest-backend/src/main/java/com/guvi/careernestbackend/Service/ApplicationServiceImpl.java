package com.guvi.careernestbackend.Service;

import com.guvi.careernestbackend.DTO.ApplyRequestDTO;
import com.guvi.careernestbackend.DTO.StatusUpdateRequestDTO;
import com.guvi.careernestbackend.Model.Application;
import com.guvi.careernestbackend.Model.ApplicationStatus;
import com.guvi.careernestbackend.Model.Job;
import com.guvi.careernestbackend.Model.JobStatus;
import com.guvi.careernestbackend.Model.Role;
import com.guvi.careernestbackend.Model.User;
import com.guvi.careernestbackend.Repository.ApplicationRepository;
import com.guvi.careernestbackend.Repository.JobRepository;
import com.guvi.careernestbackend.Repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final TwilioService twilioService; // Injected for SMS

    public ApplicationServiceImpl(ApplicationRepository applicationRepository,
                                  JobRepository jobRepository,
                                  UserRepository userRepository,
                                  TwilioService twilioService) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.twilioService = twilioService;
    }

    // 1. Apply for Job
    @Override
    public Map<String, Object> applyForJob(String jobId, ApplyRequestDTO request) {
        Map<String, Object> response = new HashMap<>();

        Job job = jobRepository.findById(jobId).orElse(null);
        if (job == null) {
            response.put("success", false);
            response.put("message", "Job not found with ID: " + jobId);
            return response;
        }

        if (job.getStatus() != JobStatus.OPEN) {
            response.put("success", false);
            response.put("message", "This job is closed for applications");
            return response;
        }

        User seeker = userRepository.findById(request.getSeekerId()).orElse(null);
        if (seeker == null) {
            response.put("success", false);
            response.put("message", "Seeker not found with ID: " + request.getSeekerId());
            return response;
        }

        if (seeker.getRole() != Role.JOB_SEEKER) {
            response.put("success", false);
            response.put("message", "Only users with role JOB_SEEKER can apply for jobs");
            return response;
        }

        if (applicationRepository.existsByJobIdAndSeekerId(jobId, request.getSeekerId())) {
            response.put("success", false);
            response.put("message", "You have already applied for this job");
            return response;
        }

        Application application = new Application();
        application.setJobId(jobId);
        application.setSeekerId(seeker.getId());
        application.setSeekerName(seeker.getFullName());
        application.setSeekerEmail(seeker.getEmail());
        application.setSeekerPhone(seeker.getPhoneNumber());
        application.setResumeUrl(request.getResumeUrl());
        application.setCoverLetter(request.getCoverLetter());
        application.setStatus(ApplicationStatus.APPLIED);

        Application savedApplication = applicationRepository.save(application);

        response.put("success", true);
        response.put("message", "Application submitted successfully");
        response.put("application", savedApplication);
        return response;
    }

    // 2. View My Applications
    @Override
    public List<Application> getApplicationsBySeeker(String seekerId) {
        return applicationRepository.findBySeekerId(seekerId);
    }

    // 3. View Applications by Job
    @Override
    public List<Application> getApplicationsByJob(String jobId) {
        return applicationRepository.findByJobId(jobId);
    }

    // 4. Update Status and Dispatch SMS
    @Override
    public Map<String, Object> updateApplicationStatus(String applicationId, StatusUpdateRequestDTO request) {
        Map<String, Object> response = new HashMap<>();

        Application application = applicationRepository.findById(applicationId).orElse(null);
        if (application == null) {
            response.put("success", false);
            response.put("message", "Application not found with ID: " + applicationId);
            return response;
        }

        // Update application status
        application.setStatus(request.getStatus());
        Application updatedApplication = applicationRepository.save(application);

        // Fetch job details for personalized SMS notification
        Job job = jobRepository.findById(application.getJobId()).orElse(null);
        String jobTitle = (job != null) ? job.getTitle() : "Job";
        String company = (job != null) ? job.getCompanyName() : "CareerNest";

        // Dispatch Twilio SMS
        String smsText = String.format(
                "Hello %s! Your application status for %s at %s has been updated to: %s.",
                application.getSeekerName(), jobTitle, company, request.getStatus()
        );
        twilioService.sendSms(application.getSeekerPhone(), smsText);

        response.put("success", true);
        response.put("message", "Application status updated to " + request.getStatus() + " and SMS notification triggered");
        response.put("application", updatedApplication);
        return response;
    }
}