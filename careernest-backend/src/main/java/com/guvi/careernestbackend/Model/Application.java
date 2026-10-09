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
@CompoundIndex(def = "{'jobId': 1, 'seekerId': 1}", unique = true) // Prevents duplicate applications to the same job
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Application {

    @Id
    private String id;

    @Indexed
    private String jobId;

    @Indexed
    private String seekerId;

    // Snapshot of seeker info at the time of application
    private String seekerName;
    private String seekerEmail;
    private String seekerPhone; // Used to trigger Twilio SMS updates

    private String resumeUrl;
    private String coverLetter;

    private ApplicationStatus status = ApplicationStatus.APPLIED;

    @CreatedDate
    private LocalDateTime appliedAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}