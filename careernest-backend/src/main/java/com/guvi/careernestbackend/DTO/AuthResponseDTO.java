package com.guvi.careernestbackend.DTO;

import com.guvi.careernestbackend.Model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponseDTO {

    private String token;
    private String id;
    private String fullName;
    private String email;
    private Role role;
    private String message;
}