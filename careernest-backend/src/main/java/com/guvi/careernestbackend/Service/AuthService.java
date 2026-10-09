package com.guvi.careernestbackend.Service;

import com.guvi.careernestbackend.DTO.LoginRequestDTO;
import com.guvi.careernestbackend.DTO.RegisterRequestDTO;
import com.guvi.careernestbackend.Model.User;
import com.guvi.careernestbackend.Repository.UserRepository;
import com.guvi.careernestbackend.Security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    // 1. Register with BCrypt Password Hashing
    public String register(RegisterRequestDTO registerRequestDTO) {
        if (userRepository.existsByEmail(registerRequestDTO.getEmail())) {
            return "Email '" + registerRequestDTO.getEmail() + "' is already registered";
        }

        User user = new User();
        user.setFullName(registerRequestDTO.getFullName());
        user.setEmail(registerRequestDTO.getEmail());
        // Hash password with BCrypt
        user.setPassword(passwordEncoder.encode(registerRequestDTO.getPassword()));
        user.setPhoneNumber(registerRequestDTO.getPhoneNumber());
        user.setRole(registerRequestDTO.getRole());
        user.setIsActive(true);

        userRepository.save(user);

        return "User registered successfully as " + user.getRole();
    }

    // 2. Login with AuthenticationManager & Return JWT Token
    public Map<String, Object> login(LoginRequestDTO loginRequestDTO) {
        // Authenticate via Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDTO.getEmail(),
                        loginRequestDTO.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtUtil.generateToken(userDetails);

        User user = userRepository.findByEmail(loginRequestDTO.getEmail()).orElse(null);

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("type", "Bearer");
        response.put("email", userDetails.getUsername());
        response.put("role", user != null ? user.getRole() : "");
        response.put("id", user != null ? user.getId() : "");
        response.put("fullName", user != null ? user.getFullName() : "");
        response.put("message", "Login successful");

        return response;
    }
}