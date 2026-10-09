package com.guvi.careernestbackend.Repository;

import com.guvi.careernestbackend.Model.Application;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends MongoRepository<Application, String> {

    // 1. Prevent duplicate applications: has this seeker already applied for this job?
    Boolean existsByJobIdAndSeekerId(String jobId, String seekerId);

    // 2. For Job Seeker: list all applications submitted by them ("My Applications")
    List<Application> findBySeekerId(String seekerId);

    // 3. For Employer: list all candidates who applied for a specific job
    List<Application> findByJobId(String jobId);
}