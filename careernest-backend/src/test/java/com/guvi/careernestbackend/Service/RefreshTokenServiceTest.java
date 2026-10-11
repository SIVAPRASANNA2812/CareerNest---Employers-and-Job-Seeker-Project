package com.guvi.careernestbackend.Service;

import com.guvi.careernestbackend.DTO.TokenRefreshRequestDTO;
import com.guvi.careernestbackend.DTO.TokenRefreshResponseDTO;
import com.guvi.careernestbackend.Exception.ResourceNotFoundException;
import com.guvi.careernestbackend.Exception.UnauthorizedActionException;
import com.guvi.careernestbackend.Model.RefreshToken;
import com.guvi.careernestbackend.Model.Role;
import com.guvi.careernestbackend.Model.User;
import com.guvi.careernestbackend.Repository.RefreshTokenRepository;
import com.guvi.careernestbackend.Repository.UserRepository;
import com.guvi.careernestbackend.Security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User testUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshExpirationMs", 604800000L); // 7 days

        testUser = new User();
        testUser.setId("user-123");
        testUser.setEmail("developer@example.com");
        testUser.setRole(Role.JOB_SEEKER);
    }

    @Test
    @DisplayName("Create Refresh Token - Deletes previous token and persists fresh UUID")
    void testCreateRefreshToken_Success() {
        RefreshToken token = RefreshToken.builder()
                .userId("user-123")
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusSeconds(3600))
                .build();

        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(token);

        RefreshToken result = refreshTokenService.createRefreshToken("user-123");

        assertNotNull(result);
        assertEquals("user-123", result.getUserId());
        assertNotNull(result.getToken());

        verify(refreshTokenRepository, times(1)).deleteByUserId("user-123");
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Rotate Refresh Token - Success invalidates old token and returns fresh token pair")
    void testRotateRefreshToken_Success() {
        String oldTokenString = "old-uuid-token";
        RefreshToken existingToken = RefreshToken.builder()
                .userId("user-123")
                .token(oldTokenString)
                .expiryDate(Instant.now().plusSeconds(3600))
                .build();

        UserDetails mockUserDetails = mock(UserDetails.class);

        when(refreshTokenRepository.findByToken(oldTokenString)).thenReturn(Optional.of(existingToken));
        when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));
        when(userDetailsService.loadUserByUsername("developer@example.com")).thenReturn(mockUserDetails);
        when(jwtUtil.generateToken(mockUserDetails)).thenReturn("fresh-new-jwt-access-token");

        RefreshToken newRefreshToken = RefreshToken.builder()
                .userId("user-123")
                .token("fresh-new-refresh-uuid")
                .expiryDate(Instant.now().plusSeconds(7200))
                .build();

        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(newRefreshToken);

        TokenRefreshRequestDTO request = new TokenRefreshRequestDTO(oldTokenString);
        TokenRefreshResponseDTO response = refreshTokenService.rotateRefreshToken(request);

        assertNotNull(response);
        assertEquals("fresh-new-jwt-access-token", response.getAccessToken());
        assertEquals("fresh-new-refresh-uuid", response.getRefreshToken());
        assertEquals("Bearer", response.getType());

        verify(refreshTokenRepository, times(1)).delete(existingToken);
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Rotate Refresh Token - Throws UnauthorizedActionException when token not found")
    void testRotateRefreshToken_InvalidToken_ThrowsUnauthorized() {
        when(refreshTokenRepository.findByToken("non-existent-token")).thenReturn(Optional.empty());

        TokenRefreshRequestDTO request = new TokenRefreshRequestDTO("non-existent-token");

        UnauthorizedActionException ex = assertThrows(UnauthorizedActionException.class, () ->
                refreshTokenService.rotateRefreshToken(request)
        );

        assertEquals("Invalid refresh token", ex.getMessage());
        verify(refreshTokenRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Rotate Refresh Token - Throws UnauthorizedActionException and deletes when expired")
    void testRotateRefreshToken_ExpiredToken_ThrowsUnauthorized() {
        String expiredTokenString = "expired-token-uuid";
        RefreshToken expiredToken = RefreshToken.builder()
                .userId("user-123")
                .token(expiredTokenString)
                .expiryDate(Instant.now().minusSeconds(100))
                .build();

        when(refreshTokenRepository.findByToken(expiredTokenString)).thenReturn(Optional.of(expiredToken));

        TokenRefreshRequestDTO request = new TokenRefreshRequestDTO(expiredTokenString);

        UnauthorizedActionException ex = assertThrows(UnauthorizedActionException.class, () ->
                refreshTokenService.rotateRefreshToken(request)
        );

        assertTrue(ex.getMessage().contains("expired"));
        verify(refreshTokenRepository, times(1)).delete(expiredToken);
    }

    @Test
    @DisplayName("Rotate Refresh Token - Throws ResourceNotFoundException when user is deleted")
    void testRotateRefreshToken_UserNotFound_Throws404() {
        String tokenString = "valid-token";
        RefreshToken token = RefreshToken.builder()
                .userId("missing-user")
                .token(tokenString)
                .expiryDate(Instant.now().plusSeconds(3600))
                .build();

        when(refreshTokenRepository.findByToken(tokenString)).thenReturn(Optional.of(token));
        when(userRepository.findById("missing-user")).thenReturn(Optional.empty());

        TokenRefreshRequestDTO request = new TokenRefreshRequestDTO(tokenString);

        assertThrows(ResourceNotFoundException.class, () ->
                refreshTokenService.rotateRefreshToken(request)
        );
    }
}