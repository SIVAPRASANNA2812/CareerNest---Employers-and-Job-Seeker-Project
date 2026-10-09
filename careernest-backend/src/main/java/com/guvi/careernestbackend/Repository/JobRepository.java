package com.guvi.careernestbackend.Repository;

import com.guvi.careernestbackend.Model.Job;
import com.guvi.careernestbackend.Model.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends MongoRepository<Job, String> {

    // For Employer
    List<Job> findByEmployerId(String employerId);

    // 1. Paginated open jobs (Default list)
    Page<Job> findByStatus(JobStatus status, Pageable pageable);

    // 2. Search by Title (Just like findByNameContainingIgnoreCase in Product)
    Page<Job> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    // 3. Filter by Location (Just like findByCategory in Product)
    Page<Job> findByLocationContainingIgnoreCase(String location, Pageable pageable);
}