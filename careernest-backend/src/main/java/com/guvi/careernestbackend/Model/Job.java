package com.guvi.careernestbackend.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "jobs")
// Compound index for ultra-fast seeker search: status + location
@CompoundIndex(name = "status_location_idx", def = "{'status': 1, 'location': 1}")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Job {

    @Id
    private String id;

    @Indexed // Fast title search lookup
    private String title;

    private String description;

    private String companyName;

    @Indexed // Fast city/region filtering
    private String location;

    private String salary;

    private LocalDate deadline;

    @Indexed // Fast employer dashboard lookup
    private String employerId;

    @Indexed // Fast open/closed status filtering
    private JobStatus status = JobStatus.OPEN;

    private String jobType;

    @CreatedDate
    @Indexed // Fast sorting by newest jobs
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}