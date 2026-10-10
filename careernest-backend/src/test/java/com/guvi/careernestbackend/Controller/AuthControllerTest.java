package com.guvi.careernestbackend.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guvi.careernestbackend.DTO.LoginRequestDTO;
import com.guvi.careernestbackend.DTO.RegisterRequestDTO;
import com.guvi.careernestbackend.DTO.TokenRefreshRequestDTO;
import com.guvi.careernestbackend.DTO.TokenRefreshResponseDTO;
import com.guvi.careernestbackend.Exception.UserAlreadyExistsException;
import com.guvi.careernestbackend.Model.Role;
import com.guvi.careernestbackend.Service.AuthService;
import com.guvi.careernestbackend.Service.RefreshTokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc; // Spring Boot 4 package
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean; // Spring Boot 4 MockitoBean
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@AutoConfigureMockMvc
@SpringBootTest(properties = "spring.data.mongodb.auto-index-creation=false")
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    // REPLACE WITH:
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private RefreshTokenService refreshTokenService;

    @Test
    @DisplayName("POST /api/auth/register - Success returns HTTP 200")
    void testRegister_Success() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO(
                "Jane Doe",
                "jane@example.com",
                "securePass123",
                "+919876543210",
                Role.JOB_SEEKER
        );

        when(authService.register(any(RegisterRequestDTO.class)))
                .thenReturn("User registered successfully as JOB_SEEKER");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully as JOB_SEEKER"));
    }

    @Test
    @DisplayName("POST /api/auth/register - Validation Failure: Short password & invalid phone returns 400 with errors map")
    void testRegister_ValidationFailure() throws Exception {
        RegisterRequestDTO invalidRequest = new RegisterRequestDTO(
                "",
                "invalid-email-format",
                "123",
                "not-a-phone",
                null
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.validationErrors.email").exists())
                .andExpect(jsonPath("$.validationErrors.password").exists())
                .andExpect(jsonPath("$.validationErrors.phoneNumber").exists());
    }

    @Test
    @DisplayName("POST /api/auth/register - Duplicate Email returns HTTP 409 Conflict")
    void testRegister_DuplicateEmail_Returns409() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO(
                "Duplicate User",
                "existing@example.com",
                "password123",
                "+919876543210",
                Role.EMPLOYER
        );

        when(authService.register(any(RegisterRequestDTO.class)))
                .thenThrow(new UserAlreadyExistsException("Email 'existing@example.com' is already registered"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Email 'existing@example.com' is already registered"));
    }

    @Test
    @DisplayName("POST /api/auth/login - Success returns JWT and RefreshToken")
    void testLogin_Success() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO("employer@techcorp.com", "password123");

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("token", "fake.access.token");
        mockResponse.put("refreshToken", "fake-refresh-uuid");
        mockResponse.put("role", "EMPLOYER");

        when(authService.login(any(LoginRequestDTO.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake.access.token"))
                .andExpect(jsonPath("$.refreshToken").value("fake-refresh-uuid"))
                .andExpect(jsonPath("$.role").value("EMPLOYER"));
    }

    @Test
    @DisplayName("POST /api/auth/refresh - Rotate Token Success returns HTTP 200")
    void testRefreshToken_Success() throws Exception {
        TokenRefreshRequestDTO refreshRequest = new TokenRefreshRequestDTO("valid-old-token-uuid");

        // Uses .type("Bearer") matching your TokenRefreshResponseDTO
        TokenRefreshResponseDTO mockResponse = TokenRefreshResponseDTO.builder()
                .accessToken("new.access.token")
                .refreshToken("new-refresh-uuid")
                .type("Bearer")
                .build();

        when(refreshTokenService.rotateRefreshToken(any(TokenRefreshRequestDTO.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new.access.token"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-uuid"));
    }
}