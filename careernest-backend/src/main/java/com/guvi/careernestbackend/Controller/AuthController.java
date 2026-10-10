package com.guvi.careernestbackend.Controller;

import com.guvi.careernestbackend.DTO.LoginRequestDTO;
import com.guvi.careernestbackend.DTO.RegisterRequestDTO;
import com.guvi.careernestbackend.DTO.TokenRefreshRequestDTO;
import com.guvi.careernestbackend.DTO.TokenRefreshResponseDTO;
import com.guvi.careernestbackend.Service.AuthService;
import com.guvi.careernestbackend.Service.RefreshTokenService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    public AuthController(AuthService authService, RefreshTokenService refreshTokenService) {
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequestDTO request) {
        String message = authService.register(request);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequestDTO request) {
        Map<String, Object> response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    // Refresh & Rotate Token Endpoint
    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponseDTO> refreshToken(@Valid @RequestBody TokenRefreshRequestDTO request) {
        TokenRefreshResponseDTO response = refreshTokenService.rotateRefreshToken(request);
        return ResponseEntity.ok(response);
    }
}