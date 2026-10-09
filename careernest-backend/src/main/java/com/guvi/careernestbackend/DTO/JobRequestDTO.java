package com.guvi.careernestbackend.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobRequestDTO {

    @NotBlank(message = "Job title is required")
    private String title;

    @NotBlank(message = "Job description is required")
    private String description;

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotBlank(message = "Location is required")
    private String location;

    @NotBlank(message = "Salary is required")
    private String salary;

    @NotNull(message = "Deadline is required")
    private LocalDate deadline;

    @NotBlank(message = "Employer ID is required")
    private String employerId; // The ID of the logged-in employer

    private String jobType; // Optional: FULL_TIME, PART_TIME, REMOTE
}