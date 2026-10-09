package com.guvi.careernestbackend.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "jobs")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Job {

    @Id
    private String id;

    private String title;

    private String description;

    private String companyName;

    @Indexed
    private String location;

    private String salary;

    private LocalDate deadline;

    @Indexed //Foreign Key pointing to the User.id who posted it (crucial for verifying that only the creator can edit/delete it).
    private String employerId;

    private JobStatus status = JobStatus.OPEN;

    private String jobType; // e.g., FULL_TIME, PART_TIME, REMOTE

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}