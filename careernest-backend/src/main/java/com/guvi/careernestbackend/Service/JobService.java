package com.guvi.careernestbackend.Service;

import com.guvi.careernestbackend.DTO.JobRequestDTO;
import com.guvi.careernestbackend.Model.Job;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface JobService {

    // 1. Employer Job Management
    Map<String, Object> createJob(JobRequestDTO request);
    List<Job> getJobsByEmployer(String employerId);
    Job getJobById(String id);
    Map<String, Object> updateJob(String id, JobRequestDTO request);
    Map<String, Object> deleteJob(String id);

    // 2. Seeker Job Browsing & Search
    Page<Job> getOpenJobs(int page, int size, String sortBy, String sortDir);
    Page<Job> searchJobsByTitle(String title, int page, int size, String sortBy, String sortDir);
    Page<Job> getJobsByLocation(String location, int page, int size, String sortBy, String sortDir);
}