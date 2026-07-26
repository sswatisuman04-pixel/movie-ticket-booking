package com.moviebooking.controller;

import com.moviebooking.dto.request.LoginRequest;
import com.moviebooking.dto.request.RegisterRequest;
import com.moviebooking.dto.response.AuthResponse;
import com.moviebooking.entity.User;
import com.moviebooking.enums.Role;
import com.moviebooking.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.createUser(request.getName(), request.getEmail(),
                request.getPassword(), Role.CUSTOMER);
        AuthResponse response = AuthResponse.builder()
                .token("jwt-token-placeholder")
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = userService.findByEmail(request.getEmail());
        // TODO: Replace with proper password verification after security implementation
        AuthResponse response = AuthResponse.builder()
                .token("jwt-token-placeholder")
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
        return ResponseEntity.ok(response);
    }
}
