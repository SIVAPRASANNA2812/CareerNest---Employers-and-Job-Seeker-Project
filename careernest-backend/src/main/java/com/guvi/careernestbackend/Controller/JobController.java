package com.guvi.careernestbackend.Controller;

import com.guvi.careernestbackend.DTO.JobRequestDTO;
import com.guvi.careernestbackend.Model.Job;
import com.guvi.careernestbackend.Service.JobService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    // 1. Post a new Job Listing
    @PostMapping
    public ResponseEntity<Map<String, Object>> createJob(@Valid @RequestBody JobRequestDTO jobRequestDTO) {
        Map<String, Object> response = this.jobService.createJob(jobRequestDTO);
        return ResponseEntity.ok(response);
    }

    // 2. View all jobs posted by a specific Employer
    @GetMapping("/employer/{employerId}")
    public ResponseEntity<List<Job>> getJobsByEmployer(@PathVariable String employerId) {
        List<Job> jobs = this.jobService.getJobsByEmployer(employerId);
        return ResponseEntity.ok(jobs);
    }

    // 3. View a single Job by ID
    @GetMapping("/{id}")
    public ResponseEntity<Job> getJobById(@PathVariable String id) {
        Job job = this.jobService.getJobById(id);
        if (job == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(job);
    }

    // 4. Update an existing Job Listing
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateJob(
            @PathVariable String id,
            @Valid @RequestBody JobRequestDTO jobRequestDTO
    ) {
        Map<String, Object> response = this.jobService.updateJob(id, jobRequestDTO);
        return ResponseEntity.ok(response);
    }

    // 5. Delete a Job Listing
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteJob(@PathVariable String id) {
        Map<String, Object> response = this.jobService.deleteJob(id);
        return ResponseEntity.ok(response);
    }

    // 6. Paginated open jobs list
    // GET /api/jobs?page=0&size=10&sortBy=createdAt&sortDir=desc
    @GetMapping
    public ResponseEntity<Page<Job>> getOpenJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Page<Job> jobs = this.jobService.getOpenJobs(page, size, sortBy, sortDir);
        return ResponseEntity.ok(jobs);
    }

    // 7. Search jobs by Title
    // GET /api/jobs/search?title=java&page=0&size=10&sortBy=createdAt&sortDir=desc
    @GetMapping("/search")
    public ResponseEntity<Page<Job>> searchJobsByTitle(
            @RequestParam String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Page<Job> jobs = this.jobService.searchJobsByTitle(title, page, size, sortBy, sortDir);
        return ResponseEntity.ok(jobs);
    }

    // 8. Filter jobs by Location
    // GET /api/jobs/location/{location}?page=0&size=10&sortBy=createdAt&sortDir=desc
    @GetMapping("/location/{location}")
    public ResponseEntity<Page<Job>> getJobsByLocation(
            @PathVariable String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Page<Job> jobs = this.jobService.getJobsByLocation(location, page, size, sortBy, sortDir);
        return ResponseEntity.ok(jobs);
    }
}