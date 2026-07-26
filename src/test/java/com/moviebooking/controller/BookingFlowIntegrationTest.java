package com.moviebooking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moviebooking.dto.request.ConfirmBookingRequest;
import com.moviebooking.dto.request.HoldSeatsRequest;
import com.moviebooking.entity.*;
import com.moviebooking.enums.*;
import com.moviebooking.repository.*;
import com.moviebooking.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BookingFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CityRepository cityRepository;
    @Autowired
    private TheaterRepository theaterRepository;
    @Autowired
    private ScreenRepository screenRepository;
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private ShowRepository showRepository;
    @Autowired
    private ShowSeatRepository showSeatRepository;
    @Autowired
    private PricingTierRepository pricingTierRepository;
    @Autowired
    private RefundPolicyRepository refundPolicyRepository;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private User customer;
    private String customerToken;
    private Show show;
    private ShowSeat showSeat;

    @BeforeEach
    void setUp() {
        // Create customer
        customer = userRepository.save(User.builder()
                .name("Customer").email("customer@booking.com")
                .password(passwordEncoder.encode("password123")).role(Role.CUSTOMER).build());
        customerToken = jwtUtil.generateToken(customer.getId(), customer.getEmail(), customer.getRole());

        // Create city → theater → screen → seat → show → showSeat
        City city = cityRepository.save(City.builder().name("TestCity").build());
        Theater theater = theaterRepository.save(Theater.builder().name("TestTheater").address("123 St").city(city).build());
        Screen screen = screenRepository.save(Screen.builder().name("Screen 1").theater(theater).totalSeats(50).build());
        Seat seat = seatRepository.save(Seat.builder().screen(screen).row("A").number(1).seatType(SeatType.REGULAR).build());

        show = showRepository.save(Show.builder().movieName("Avengers")
                .screen(screen).date(LocalDate.now().plusDays(5))
                .startTime(LocalTime.of(14, 0)).endTime(LocalTime.of(16, 30)).build());

        showSeat = showSeatRepository.save(ShowSeat.builder()
                .show(show).seat(seat).status(ShowSeatStatus.AVAILABLE)
                .price(new BigDecimal("300.00")).build());

        // Refund policy — full refund if > 24 hours
        refundPolicyRepository.save(RefundPolicy.builder()
                .name("Full Refund").theater(theater)
                .hoursBeforeShow(24).refundPercentage(100).build());
    }

    @Test
    void fullBookingFlow_holdConfirmCancel_shouldSucceed() throws Exception {
        // Step 1: Hold seats
        HoldSeatsRequest holdRequest = new HoldSeatsRequest();
        holdRequest.setShowId(show.getId());
        holdRequest.setShowSeatIds(List.of(showSeat.getId()));

        mockMvc.perform(post("/api/bookings/hold")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(holdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.holdExpiresAt", notNullValue()))
                .andExpect(jsonPath("$.message", is("Seats held successfully")));

        // Verify seat is HELD
        ShowSeat held = showSeatRepository.findById(showSeat.getId()).orElseThrow();
        assertThat(held.getStatus()).isEqualTo(ShowSeatStatus.HELD);

        // Step 2: Confirm booking
        ConfirmBookingRequest confirmRequest = new ConfirmBookingRequest();
        confirmRequest.setShowId(show.getId());
        confirmRequest.setShowSeatIds(List.of(showSeat.getId()));

        MvcResult confirmResult = mockMvc.perform(post("/api/bookings/confirm")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("CONFIRMED")))
                .andExpect(jsonPath("$.totalAmount").value(closeTo(300.0, 0.01)))
                .andReturn();

        // Verify seat is BOOKED
        ShowSeat booked = showSeatRepository.findById(showSeat.getId()).orElseThrow();
        assertThat(booked.getStatus()).isEqualTo(ShowSeatStatus.BOOKED);

        // Extract bookingId
        String json = confirmResult.getResponse().getContentAsString();
        Long bookingId = objectMapper.readTree(json).get("id").asLong();

        // Step 3: Cancel booking
        mockMvc.perform(post("/api/bookings/" + bookingId + "/cancel")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refundAmount").value(closeTo(300.0, 0.01)))
                .andExpect(jsonPath("$.message", containsString("cancelled")));

        // Verify seat is released back to AVAILABLE
        ShowSeat released = showSeatRepository.findById(showSeat.getId()).orElseThrow();
        assertThat(released.getStatus()).isEqualTo(ShowSeatStatus.AVAILABLE);
    }

    @Test
    void holdSeats_unauthenticated_shouldReturn403() throws Exception {
        HoldSeatsRequest holdRequest = new HoldSeatsRequest();
        holdRequest.setShowId(show.getId());
        holdRequest.setShowSeatIds(List.of(showSeat.getId()));

        mockMvc.perform(post("/api/bookings/hold")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(holdRequest)))
                .andExpect(status().isForbidden());
    }
}
