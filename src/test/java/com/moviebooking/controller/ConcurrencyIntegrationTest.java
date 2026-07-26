package com.moviebooking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ConcurrencyIntegrationTest {

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
    private JwtUtil jwtUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private Show show;
    private ShowSeat showSeat;
    private String user1Token;
    private String user2Token;

    @BeforeEach
    void setUp() {
        User user1 = userRepository.save(User.builder()
                .name("User1").email("user1@test.com")
                .password(passwordEncoder.encode("pass123")).role(Role.CUSTOMER).build());
        User user2 = userRepository.save(User.builder()
                .name("User2").email("user2@test.com")
                .password(passwordEncoder.encode("pass123")).role(Role.CUSTOMER).build());

        user1Token = jwtUtil.generateToken(user1.getId(), user1.getEmail(), user1.getRole());
        user2Token = jwtUtil.generateToken(user2.getId(), user2.getEmail(), user2.getRole());

        City city = cityRepository.save(City.builder().name("ConcCity").build());
        Theater theater = theaterRepository.save(Theater.builder().name("ConcTheater").address("1 St").city(city).build());
        Screen screen = screenRepository.save(Screen.builder().name("S1").theater(theater).totalSeats(10).build());
        Seat seat = seatRepository.save(Seat.builder().screen(screen).row("A").number(1).seatType(SeatType.REGULAR).build());

        show = showRepository.save(Show.builder().movieName("ConcMovie")
                .screen(screen).date(LocalDate.now().plusDays(5))
                .startTime(LocalTime.of(18, 0)).endTime(LocalTime.of(20, 0)).build());

        showSeat = showSeatRepository.save(ShowSeat.builder()
                .show(show).seat(seat).status(ShowSeatStatus.AVAILABLE)
                .price(new BigDecimal("250.00")).build());
    }

    @Test
    void twoUsersHoldSameSeat_oneShouldFail() throws Exception {
        HoldSeatsRequest request = new HoldSeatsRequest();
        request.setShowId(show.getId());
        request.setShowSeatIds(List.of(showSeat.getId()));

        // First user holds the seat — should succeed
        MvcResult result1 = mockMvc.perform(post("/api/bookings/hold")
                        .header("Authorization", "Bearer " + user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        // Second user tries to hold same seat — should fail with 409
        MvcResult result2 = mockMvc.perform(post("/api/bookings/hold")
                        .header("Authorization", "Bearer " + user2Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        // One should be 200, the other should be 409 (conflict/seat unavailable)
        int status1 = result1.getResponse().getStatus();
        int status2 = result2.getResponse().getStatus();

        assertThat(status1).isEqualTo(200);
        assertThat(status2).isEqualTo(409);
    }
}
