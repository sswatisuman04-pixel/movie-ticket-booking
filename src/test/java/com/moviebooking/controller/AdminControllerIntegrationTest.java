package com.moviebooking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moviebooking.dto.request.CreateCityRequest;
import com.moviebooking.entity.User;
import com.moviebooking.enums.Role;
import com.moviebooking.repository.UserRepository;
import com.moviebooking.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String customerToken;

    @BeforeEach
    void setUp() {
        User admin = userRepository.save(User.builder()
                .name("Admin User").email("adminit@test.com")
                .password(passwordEncoder.encode("admin123")).role(Role.ADMIN).build());
        adminToken = jwtUtil.generateToken(admin.getId(), admin.getEmail(), admin.getRole());

        User customer = userRepository.save(User.builder()
                .name("Customer User").email("customerit@test.com")
                .password(passwordEncoder.encode("cust123")).role(Role.CUSTOMER).build());
        customerToken = jwtUtil.generateToken(customer.getId(), customer.getEmail(), customer.getRole());
    }

    @Test
    void createCity_asAdmin_shouldReturn201() throws Exception {
        CreateCityRequest request = new CreateCityRequest();
        request.setName("New Delhi");

        mockMvc.perform(post("/api/admin/cities")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("New Delhi")));
    }

    @Test
    void createCity_asCustomer_shouldReturn403() throws Exception {
        CreateCityRequest request = new CreateCityRequest();
        request.setName("Bangalore");

        mockMvc.perform(post("/api/admin/cities")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createCity_unauthenticated_shouldReturn403() throws Exception {
        CreateCityRequest request = new CreateCityRequest();
        request.setName("Chennai");

        mockMvc.perform(post("/api/admin/cities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
