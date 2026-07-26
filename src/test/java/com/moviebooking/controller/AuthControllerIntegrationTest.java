package com.moviebooking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moviebooking.dto.request.LoginRequest;
import com.moviebooking.dto.request.RegisterRequest;
import com.moviebooking.entity.User;
import com.moviebooking.enums.Role;
import com.moviebooking.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void register_withValidData_shouldReturn201AndToken() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Test User");
        request.setEmail("newuser@test.com");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.email", is("newuser@test.com")))
                .andExpect(jsonPath("$.role", is("CUSTOMER")));
    }

    @Test
    void register_withDuplicateEmail_shouldReturn409() throws Exception {
        // Pre-create a user
        User existingUser = User.builder()
                .name("Existing").email("existing@test.com")
                .password(passwordEncoder.encode("pass123")).role(Role.CUSTOMER).build();
        userRepository.save(existingUser);

        RegisterRequest request = new RegisterRequest();
        request.setName("Another User");
        request.setEmail("existing@test.com");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void login_withValidCredentials_shouldReturn200AndToken() throws Exception {
        // Create a user with known password
        User user = User.builder()
                .name("Login User").email("login@test.com")
                .password(passwordEncoder.encode("mypassword")).role(Role.CUSTOMER).build();
        userRepository.save(user);

        LoginRequest request = new LoginRequest();
        request.setEmail("login@test.com");
        request.setPassword("mypassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.email", is("login@test.com")))
                .andExpect(jsonPath("$.role", is("CUSTOMER")));
    }

    @Test
    void login_withInvalidPassword_shouldReturn401() throws Exception {
        // Create a user
        User user = User.builder()
                .name("User").email("badlogin@test.com")
                .password(passwordEncoder.encode("correctpass")).role(Role.CUSTOMER).build();
        userRepository.save(user);

        LoginRequest request = new LoginRequest();
        request.setEmail("badlogin@test.com");
        request.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
