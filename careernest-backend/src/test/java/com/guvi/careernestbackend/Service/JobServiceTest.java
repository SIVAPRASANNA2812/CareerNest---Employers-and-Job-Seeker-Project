package com.guvi.careernestbackend.Service;

import com.guvi.careernestbackend.DTO.JobRequestDTO;
import com.guvi.careernestbackend.Exception.ResourceNotFoundException;
import com.guvi.careernestbackend.Exception.UnauthorizedActionException;
import com.guvi.careernestbackend.Model.Job;
import com.guvi.careernestbackend.Model.JobStatus;
import com.guvi.careernestbackend.Model.Role;
import com.guvi.careernestbackend.Model.User;
import com.guvi.careernestbackend.Repository.JobRepository;
import com.guvi.careernestbackend.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JobServiceImpl jobService;

    private User employer;
    private User jobSeeker;
    private JobRequestDTO jobRequest;

    @BeforeEach
    void setUp() {
        employer = new User();
        employer.setId("emp-123");
        employer.setRole(Role.EMPLOYER);
        employer.setEmail("hr@google.com");

        jobSeeker = new User();
        jobSeeker.setId("seeker-456");
        jobSeeker.setRole(Role.JOB_SEEKER);

        jobRequest = new JobRequestDTO();
        jobRequest.setTitle("Backend Engineer");
        jobRequest.setDescription("Deep Java experience");
        jobRequest.setCompanyName("Google");
        jobRequest.setLocation("Bangalore");
        jobRequest.setSalary("25 LPA");
        jobRequest.setDeadline(LocalDate.now().plusMonths(1));
        jobRequest.setEmployerId("emp-123");
    }

    @Test
    @DisplayName("Create Job - Success when user is EMPLOYER")
    void testCreateJob_Success() {
        when(userRepository.findById("emp-123")).thenReturn(Optional.of(employer));

        Job savedJob = new Job();
        savedJob.setId("job-999");
        savedJob.setTitle("Backend Engineer");
        savedJob.setStatus(JobStatus.OPEN);

        when(jobRepository.save(any(Job.class))).thenReturn(savedJob);

        Map<String, Object> response = jobService.createJob(jobRequest);

        assertTrue((Boolean) response.get("success"));
        assertEquals("Job posted successfully", response.get("message"));
        verify(jobRepository, times(1)).save(any(Job.class));
    }

    @Test
    @DisplayName("Create Job - Throws ResourceNotFoundException when Employer does not exist")
    void testCreateJob_EmployerNotFound_Throws404() {
        when(userRepository.findById("emp-123")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> jobService.createJob(jobRequest));
        verify(jobRepository, never()).save(any(Job.class));
    }

    @Test
    @DisplayName("Create Job - Throws UnauthorizedActionException when user role is JOB_SEEKER")
    void testCreateJob_SeekerCannotPost_Throws403() {
        when(userRepository.findById("emp-123")).thenReturn(Optional.of(jobSeeker));

        assertThrows(UnauthorizedActionException.class, () -> jobService.createJob(jobRequest));
        verify(jobRepository, never()).save(any(Job.class));
    }

    @Test
    @DisplayName("Delete Job - Throws ResourceNotFoundException if Job ID is invalid")
    void testDeleteJob_NotFound_Throws404() {
        when(jobRepository.existsById("invalid-id")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> jobService.deleteJob("invalid-id"));
        verify(jobRepository, never()).deleteById(anyString());
    }
}