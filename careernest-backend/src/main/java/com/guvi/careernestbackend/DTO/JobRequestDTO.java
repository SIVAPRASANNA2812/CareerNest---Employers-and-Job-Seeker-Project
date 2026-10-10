package com.guvi.careernestbackend.DTO;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobRequestDTO {

    @NotBlank(message = "Job title is required")
    @Size(min = 3, max = 100, message = "Job title must be between 3 and 100 characters")
    private String title;

    @NotBlank(message = "Job description is required")
    @Size(min = 10, message = "Job description must be at least 10 characters")
    private String description;

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotBlank(message = "Location is required")
    private String location;

    @NotBlank(message = "Salary details are required")
    private String salary;

    @NotNull(message = "Application deadline is required")
    @FutureOrPresent(message = "Application deadline must be today or a future date")
    private LocalDate deadline;

    @NotBlank(message = "Employer ID is required")
    private String employerId;

    private String jobType; // e.g., FULL_TIME, REMOTE, INTERNSHIP
}