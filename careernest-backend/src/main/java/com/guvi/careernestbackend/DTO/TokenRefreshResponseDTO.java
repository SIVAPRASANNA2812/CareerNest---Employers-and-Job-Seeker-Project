package com.guvi.careernestbackend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TokenRefreshResponseDTO {

    private String accessToken;
    private String refreshToken;
    private String type = "Bearer";
}