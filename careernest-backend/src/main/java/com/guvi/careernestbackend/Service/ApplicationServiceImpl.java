package com.guvi.careernestbackend.Service;

import com.guvi.careernestbackend.DTO.ApplyRequestDTO;
import com.guvi.careernestbackend.DTO.StatusUpdateRequestDTO;
import com.guvi.careernestbackend.Exception.DuplicateApplicationException;
import com.guvi.careernestbackend.Exception.ResourceNotFoundException;
import com.guvi.careernestbackend.Exception.UnauthorizedActionException;
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
    private final TwilioService twilioService;

    public ApplicationServiceImpl(ApplicationRepository applicationRepository,
                                  JobRepository jobRepository,
                                  UserRepository userRepository,
                                  TwilioService twilioService) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.twilioService = twilioService;
    }

    // 1. Seeker applies for a job
    @Override
    public Map<String, Object> applyForJob(String jobId, ApplyRequestDTO request) {
        // Throws 404 if Job doesn't exist
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + jobId));

        // Throws 403 if Job is closed
        if (job.getStatus() != JobStatus.OPEN) {
            throw new UnauthorizedActionException("This job is closed for applications");
        }

        // Throws 404 if Seeker doesn't exist
        User seeker = userRepository.findById(request.getSeekerId())
                .orElseThrow(() -> new ResourceNotFoundException("Seeker not found with ID: " + request.getSeekerId()));

        // Throws 403 if role is not JOB_SEEKER
        if (seeker.getRole() != Role.JOB_SEEKER) {
            throw new UnauthorizedActionException("Only users with role JOB_SEEKER can apply for jobs");
        }

        // Throws 409 Conflict if already applied (duplicate prevention)
        if (applicationRepository.existsByJobIdAndSeekerId(jobId, request.getSeekerId())) {
            throw new DuplicateApplicationException("You have already applied for this job");
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

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Application submitted successfully");
        response.put("application", savedApplication);
        return response;
    }

    // 2. Seeker views their submitted applications
    @Override
    public List<Application> getApplicationsBySeeker(String seekerId) {
        return applicationRepository.findBySeekerId(seekerId);
    }

    // 3. Employer views all applicants for a job
    @Override
    public List<Application> getApplicationsByJob(String jobId) {
        return applicationRepository.findByJobId(jobId);
    }

    // 4. Update Status & Dispatch SMS (Throws 404 if application doesn't exist)
    @Override
    public Map<String, Object> updateApplicationStatus(String applicationId, StatusUpdateRequestDTO request) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + applicationId));

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

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Application status updated to " + request.getStatus() + " and SMS notification triggered");
        response.put("application", updatedApplication);
        return response;
    }
}