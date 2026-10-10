package com.guvi.careernestbackend.Service;

import com.guvi.careernestbackend.DTO.TokenRefreshRequestDTO;
import com.guvi.careernestbackend.DTO.TokenRefreshResponseDTO;
import com.guvi.careernestbackend.Exception.ResourceNotFoundException;
import com.guvi.careernestbackend.Exception.UnauthorizedActionException;
import com.guvi.careernestbackend.Model.RefreshToken;
import com.guvi.careernestbackend.Model.User;
import com.guvi.careernestbackend.Repository.RefreshTokenRepository;
import com.guvi.careernestbackend.Repository.UserRepository;
import com.guvi.careernestbackend.Security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Value("${jwt.refresh-expiration:604800000}") // 7 days in milliseconds
    private long refreshExpirationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                               UserRepository userRepository,
                               CustomUserDetailsService userDetailsService,
                               JwtUtil jwtUtil) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
    }

    // 1. Create a new Refresh Token upon successful login
    public RefreshToken createRefreshToken(String userId) {
        // Remove any old refresh token for this user
        refreshTokenRepository.deleteByUserId(userId);

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userId)
                .token(UUID.randomUUID().toString()) // Secure UUID
                .expiryDate(Instant.now().plusMillis(refreshExpirationMs))
                .createdAt(Instant.now())
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    // 2. Rotate Token: Validates, deletes the old one, and generates fresh pair
    public TokenRefreshResponseDTO rotateRefreshToken(TokenRefreshRequestDTO request) {
        RefreshToken token = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new UnauthorizedActionException("Invalid refresh token"));

        // Check if token has expired
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new UnauthorizedActionException("Refresh token was expired. Please make a new login request");
        }

        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found for refresh token"));

        // Generate new Access Token
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String newAccessToken = jwtUtil.generateToken(userDetails);

        // ROTATION: Delete the old refresh token and create a brand new one
        refreshTokenRepository.delete(token);
        RefreshToken newRefreshToken = createRefreshToken(user.getId());

        return TokenRefreshResponseDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .type("Bearer")
                .build();
    }
}