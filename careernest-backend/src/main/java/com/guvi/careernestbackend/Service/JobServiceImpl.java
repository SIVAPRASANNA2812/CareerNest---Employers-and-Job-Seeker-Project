package com.guvi.careernestbackend.Service;

import com.guvi.careernestbackend.DTO.JobRequestDTO;
import com.guvi.careernestbackend.Model.Job;
import com.guvi.careernestbackend.Model.JobStatus;
import com.guvi.careernestbackend.Model.Role;
import com.guvi.careernestbackend.Model.User;
import com.guvi.careernestbackend.Repository.JobRepository;
import com.guvi.careernestbackend.Repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    public JobServiceImpl(JobRepository jobRepository, UserRepository userRepository) {
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
    }

    // 1. Create a new Job Listing
    @Override
    public Map<String, Object> createJob(JobRequestDTO request) {
        Map<String, Object> response = new HashMap<>();

        // Production Check: Ensure the user exists and is an EMPLOYER
        User employer = userRepository.findById(request.getEmployerId()).orElse(null);
        if (employer == null) {
            response.put("success", false);
            response.put("message", "Employer not found with ID: " + request.getEmployerId());
            return response;
        }

        if (employer.getRole() != Role.EMPLOYER) {
            response.put("success", false);
            response.put("message", "Only users with role EMPLOYER are allowed to post jobs");
            return response;
        }

        // Map DTO to Job Model
        Job job = new Job();
        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setCompanyName(request.getCompanyName());
        job.setLocation(request.getLocation());
        job.setSalary(request.getSalary());
        job.setDeadline(request.getDeadline());
        job.setEmployerId(request.getEmployerId());
        job.setJobType(request.getJobType());
        job.setStatus(JobStatus.OPEN);

        Job savedJob = jobRepository.save(job);

        response.put("success", true);
        response.put("message", "Job posted successfully");
        response.put("job", savedJob);
        return response;
    }

    // 2. Get all jobs posted by a specific employer
    @Override
    public List<Job> getJobsByEmployer(String employerId) {
        return jobRepository.findByEmployerId(employerId);
    }

    // 3. Get single job by ID
    @Override
    public Job getJobById(String id) {
        return jobRepository.findById(id).orElse(null);
    }

    // 4. Update an existing Job Listing
    @Override
    public Map<String, Object> updateJob(String id, JobRequestDTO request) {
        Map<String, Object> response = new HashMap<>();

        Job existingJob = jobRepository.findById(id).orElse(null);
        if (existingJob == null) {
            response.put("success", false);
            response.put("message", "Job not found with ID: " + id);
            return response;
        }

        // Update fields
        existingJob.setTitle(request.getTitle());
        existingJob.setDescription(request.getDescription());
        existingJob.setCompanyName(request.getCompanyName());
        existingJob.setLocation(request.getLocation());
        existingJob.setSalary(request.getSalary());
        existingJob.setDeadline(request.getDeadline());
        existingJob.setJobType(request.getJobType());

        Job updatedJob = jobRepository.save(existingJob);

        response.put("success", true);
        response.put("message", "Job updated successfully");
        response.put("job", updatedJob);
        return response;
    }

    // 5. Delete a Job Listing
    @Override
    public Map<String, Object> deleteJob(String id) {
        Map<String, Object> response = new HashMap<>();

        if (!jobRepository.existsById(id)) {
            response.put("success", false);
            response.put("message", "Job not found with ID: " + id);
            return response;
        }

        jobRepository.deleteById(id);
        response.put("success", true);
        response.put("message", "Job deleted successfully");
        return response;
    }

    // 6. Paginated Open Jobs
    @Override
    public Page<Job> getOpenJobs(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return jobRepository.findByStatus(JobStatus.OPEN, pageable);
    }

    // 7. Search Jobs by Title
    @Override
    public Page<Job> searchJobsByTitle(String title, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return jobRepository.findByTitleContainingIgnoreCase(title, pageable);
    }

    // 8. Filter Jobs by Location
    @Override
    public Page<Job> getJobsByLocation(String location, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return jobRepository.findByLocationContainingIgnoreCase(location, pageable);
    }
}