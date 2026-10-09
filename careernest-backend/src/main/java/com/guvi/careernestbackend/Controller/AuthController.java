package com.guvi.careernestbackend.Controller;

import com.guvi.careernestbackend.DTO.LoginRequestDTO;
import com.guvi.careernestbackend.DTO.RegisterRequestDTO;
import com.guvi.careernestbackend.Service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    // Only AuthService is needed here
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // Supports both /api/auth/register and /api/auth/signup
    @PostMapping({"/register", "/signup"})
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequestDTO registerRequestDTO) {
        String response = this.authService.register(registerRequestDTO);
        return ResponseEntity.ok(response);
    }

    // Handles /api/auth/login and returns JWT token map
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequestDTO loginRequestDTO) {
        Map<String, Object> response = this.authService.login(loginRequestDTO);
        return ResponseEntity.ok(response);
    }
}