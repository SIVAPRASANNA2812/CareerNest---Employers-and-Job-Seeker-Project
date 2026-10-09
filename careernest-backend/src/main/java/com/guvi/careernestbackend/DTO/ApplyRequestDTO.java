package com.guvi.careernestbackend.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplyRequestDTO {

    @NotBlank(message = "Seeker ID is required")
    private String seekerId;

    @NotBlank(message = "Resume URL is required")
    private String resumeUrl; // e.g. Link to Google Drive, LinkedIn, or Cloud resume

    private String coverLetter; // Optional note to the employer
}