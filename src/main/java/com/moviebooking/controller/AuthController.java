package com.moviebooking.controller;

import com.moviebooking.dto.request.LoginRequest;
import com.moviebooking.dto.request.RegisterRequest;
import com.moviebooking.dto.response.AuthResponse;
import com.moviebooking.entity.User;
import com.moviebooking.enums.Role;
import com.moviebooking.exception.DuplicateResourceException;
import com.moviebooking.exception.UnauthorizedException;
import com.moviebooking.security.JwtUtil;
import com.moviebooking.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserService userService, JwtUtil jwtUtil,
                          PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (userService.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User with email '" + request.getEmail() + "' already exists");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = userService.createUser(request.getName(), request.getEmail(),
                encodedPassword, Role.CUSTOMER);

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole());

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (AuthenticationException e) {
            throw new UnauthorizedException("Invalid credentials");
        }

        User user = userService.findByEmail(request.getEmail());
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole());

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
        return ResponseEntity.ok(response);
    }
}
