package com.guvi.careernestbackend.DTO;

import com.guvi.careernestbackend.Model.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatusUpdateRequestDTO {

    @NotNull(message = "Status is required (UNDER_REVIEW, ACCEPTED, or REJECTED)")
    private ApplicationStatus status;
}