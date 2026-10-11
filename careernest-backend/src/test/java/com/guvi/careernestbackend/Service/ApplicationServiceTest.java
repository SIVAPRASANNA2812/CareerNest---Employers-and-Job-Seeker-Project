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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TwilioService twilioService;

    @InjectMocks
    private ApplicationServiceImpl applicationService;

    private Job openJob;
    private Job closedJob;
    private User jobSeeker;
    private User employer;
    private ApplyRequestDTO applyRequest;

    @BeforeEach
    void setUp() {
        openJob = new Job();
        openJob.setId("job-101");
        openJob.setTitle("Senior Java Developer");
        openJob.setCompanyName("TechCorp");
        openJob.setStatus(JobStatus.OPEN);

        closedJob = new Job();
        closedJob.setId("job-102");
        closedJob.setTitle("Frontend Intern");
        closedJob.setStatus(JobStatus.CLOSED);

        jobSeeker = new User();
        jobSeeker.setId("seeker-201");
        jobSeeker.setFullName("John Seeker");
        jobSeeker.setEmail("john@example.com");
        jobSeeker.setPhoneNumber("+919876543210");
        jobSeeker.setRole(Role.JOB_SEEKER);

        employer = new User();
        employer.setId("emp-301");
        employer.setRole(Role.EMPLOYER);

        applyRequest = new ApplyRequestDTO();
        applyRequest.setSeekerId("seeker-201");
        applyRequest.setResumeUrl("https://storage.googleapis.com/resumes/john.pdf");
        applyRequest.setCoverLetter("Passionate full-stack developer with 3 years experience.");
    }

    @Test
    @DisplayName("Apply Job - Success returns 200 with persisted application")
    void testApplyForJob_Success() {
        when(jobRepository.findById("job-101")).thenReturn(Optional.of(openJob));
        when(userRepository.findById("seeker-201")).thenReturn(Optional.of(jobSeeker));
        when(applicationRepository.existsByJobIdAndSeekerId("job-101", "seeker-201")).thenReturn(false);

        Application savedApp = new Application();
        savedApp.setId("app-501");
        savedApp.setJobId("job-101");
        savedApp.setSeekerId("seeker-201");
        savedApp.setStatus(ApplicationStatus.APPLIED);

        when(applicationRepository.save(any(Application.class))).thenReturn(savedApp);

        Map<String, Object> response = applicationService.applyForJob("job-101", applyRequest);

        assertNotNull(response);
        assertTrue((Boolean) response.get("success"));
        assertEquals("Application submitted successfully", response.get("message"));
        assertEquals(savedApp, response.get("application"));

        verify(applicationRepository, times(1)).save(any(Application.class));
    }

    @Test
    @DisplayName("Apply Job - Throws ResourceNotFoundException when Job does not exist")
    void testApplyForJob_JobNotFound_Throws404() {
        when(jobRepository.findById("non-existent-job")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                applicationService.applyForJob("non-existent-job", applyRequest)
        );

        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    @DisplayName("Apply Job - Throws UnauthorizedActionException when Job status is CLOSED")
    void testApplyForJob_JobClosed_Throws403() {
        when(jobRepository.findById("job-102")).thenReturn(Optional.of(closedJob));

        UnauthorizedActionException ex = assertThrows(UnauthorizedActionException.class, () ->
                applicationService.applyForJob("job-102", applyRequest)
        );

        assertEquals("This job is closed for applications", ex.getMessage());
        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    @DisplayName("Apply Job - Throws ResourceNotFoundException when Seeker does not exist")
    void testApplyForJob_SeekerNotFound_Throws404() {
        when(jobRepository.findById("job-101")).thenReturn(Optional.of(openJob));
        when(userRepository.findById("seeker-201")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                applicationService.applyForJob("job-101", applyRequest)
        );

        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    @DisplayName("Apply Job - Throws UnauthorizedActionException when user is not JOB_SEEKER")
    void testApplyForJob_EmployerCannotApply_Throws403() {
        when(jobRepository.findById("job-101")).thenReturn(Optional.of(openJob));
        when(userRepository.findById("emp-301")).thenReturn(Optional.of(employer));

        applyRequest.setSeekerId("emp-301");

        UnauthorizedActionException ex = assertThrows(UnauthorizedActionException.class, () ->
                applicationService.applyForJob("job-101", applyRequest)
        );

        assertEquals("Only users with role JOB_SEEKER can apply for jobs", ex.getMessage());
        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    @DisplayName("Apply Job - Throws DuplicateApplicationException when already applied")
    void testApplyForJob_AlreadyApplied_Throws409() {
        when(jobRepository.findById("job-101")).thenReturn(Optional.of(openJob));
        when(userRepository.findById("seeker-201")).thenReturn(Optional.of(jobSeeker));
        when(applicationRepository.existsByJobIdAndSeekerId("job-101", "seeker-201")).thenReturn(true);

        DuplicateApplicationException ex = assertThrows(DuplicateApplicationException.class, () ->
                applicationService.applyForJob("job-101", applyRequest)
        );

        assertEquals("You have already applied for this job", ex.getMessage());
        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    @DisplayName("Get Applications by Seeker - Returns list of candidate applications")
    void testGetApplicationsBySeeker_Success() {
        Application app = new Application();
        app.setId("app-1");
        app.setSeekerId("seeker-201");

        when(applicationRepository.findBySeekerId("seeker-201")).thenReturn(List.of(app));

        List<Application> result = applicationService.getApplicationsBySeeker("seeker-201");

        assertEquals(1, result.size());
        assertEquals("app-1", result.get(0).getId());
        verify(applicationRepository, times(1)).findBySeekerId("seeker-201");
    }

    @Test
    @DisplayName("Get Applications by Job - Returns list of job applicants")
    void testGetApplicationsByJob_Success() {
        Application app = new Application();
        app.setId("app-2");
        app.setJobId("job-101");

        when(applicationRepository.findByJobId("job-101")).thenReturn(List.of(app));

        List<Application> result = applicationService.getApplicationsByJob("job-101");

        assertEquals(1, result.size());
        assertEquals("app-2", result.get(0).getId());
        verify(applicationRepository, times(1)).findByJobId("job-101");
    }

    @Test
    @DisplayName("Update Status - Success updates status and triggers Twilio SMS")
    void testUpdateApplicationStatus_Success_SendsSms() {
        Application existingApp = new Application();
        existingApp.setId("app-501");
        existingApp.setJobId("job-101");
        existingApp.setSeekerName("John Seeker");
        existingApp.setSeekerPhone("+919876543210");
        existingApp.setStatus(ApplicationStatus.APPLIED);

        when(applicationRepository.findById("app-501")).thenReturn(Optional.of(existingApp));
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jobRepository.findById("job-101")).thenReturn(Optional.of(openJob));

        StatusUpdateRequestDTO statusRequest = new StatusUpdateRequestDTO();
        statusRequest.setStatus(ApplicationStatus.ACCEPTED);

        Map<String, Object> response = applicationService.updateApplicationStatus("app-501", statusRequest);

        assertTrue((Boolean) response.get("success"));
        assertEquals(ApplicationStatus.ACCEPTED, existingApp.getStatus());

        // Verify Twilio SMS triggered with seeker phone number
        verify(twilioService, times(1)).sendSms(eq("+919876543210"), contains("ACCEPTED"));
        verify(applicationRepository, times(1)).save(existingApp);
    }

    @Test
    @DisplayName("Update Status - Throws ResourceNotFoundException when application does not exist")
    void testUpdateApplicationStatus_NotFound_Throws404() {
        when(applicationRepository.findById("missing-app")).thenReturn(Optional.empty());

        StatusUpdateRequestDTO statusRequest = new StatusUpdateRequestDTO();
        statusRequest.setStatus(ApplicationStatus.REJECTED);

        assertThrows(ResourceNotFoundException.class, () ->
                applicationService.updateApplicationStatus("missing-app", statusRequest)
        );

        verify(twilioService, never()).sendSms(anyString(), anyString());
        verify(applicationRepository, never()).save(any(Application.class));
    }
}