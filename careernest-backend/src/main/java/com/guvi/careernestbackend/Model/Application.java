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

import java.time.LocalDateTime;

@Document(collection = "applications")
// Compound unique index preventing duplicate applications to the same job
// Line 17 in Application.java:
@CompoundIndex(def = "{'jobId': 1, 'seekerId': 1}", unique = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Application {

    @Id
    private String id;

    @Indexed // Fast lookup of all applicants for a job
    private String jobId;

    @Indexed // Fast lookup of "My Applications" for a candidate
    private String seekerId;

    private String seekerName;
    private String seekerEmail;
    private String seekerPhone;

    private String resumeUrl;
    private String coverLetter;

    @Indexed // Fast filtering by status (e.g. view only ACCEPTED candidates)
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    @CreatedDate
    private LocalDateTime appliedAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}