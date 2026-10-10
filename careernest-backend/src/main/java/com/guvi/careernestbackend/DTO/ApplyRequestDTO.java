package com.guvi.careernestbackend.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
    @Pattern(
            regexp = "^(https?://).+",
            message = "Resume URL must be a valid HTTP or HTTPS web link"
    )
    private String resumeUrl;

    private String coverLetter;
}