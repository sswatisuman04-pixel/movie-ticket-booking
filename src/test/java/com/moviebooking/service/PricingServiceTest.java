package com.moviebooking.service;

import com.moviebooking.entity.*;
import com.moviebooking.enums.SeatType;
import com.moviebooking.repository.PricingTierRepository;
import com.moviebooking.repository.TheaterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PricingServiceTest {

    @Mock
    private PricingTierRepository pricingTierRepository;
    @Mock
    private TheaterRepository theaterRepository;

    @InjectMocks
    private PricingService pricingService;

    private Seat testSeat;
    private Show testShow;
    private Theater theater;

    @BeforeEach
    void setUp() {
        City city = City.builder().id(1L).name("Mumbai").build();
        theater = Theater.builder().id(1L).name("PVR").address("123 St").city(city).build();
        Screen screen = Screen.builder().id(1L).name("Screen 1").theater(theater).totalSeats(100).build();
        testSeat = Seat.builder().id(1L).screen(screen).row("A").number(1).seatType(SeatType.REGULAR).build();

        // Use a Monday date for predictable tests
        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);
        testShow = Show.builder().id(1L).movieName("Movie").screen(screen)
                .date(monday).startTime(LocalTime.of(14, 0)).endTime(LocalTime.of(16, 0)).build();
    }

    @Test
    void calculatePrice_withMatchingTier_shouldReturnCalculatedPrice() {
        PricingTier tier = PricingTier.builder().id(1L).name("Weekday Regular")
                .theater(theater).seatType(SeatType.REGULAR)
                .basePrice(new BigDecimal("200.00")).multiplier(new BigDecimal("1.50"))
                .applicableDays("MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY").build();

        when(pricingTierRepository.findByTheaterIdAndSeatType(1L, SeatType.REGULAR))
                .thenReturn(List.of(tier));

        BigDecimal result = pricingService.calculatePrice(testSeat, testShow);

        // 200 * 1.50 = 300.00
        assertThat(result).isEqualByComparingTo("300.00");
    }

    @Test
    void calculatePrice_withNoMatchingTier_shouldReturnDefaultPrice() {
        when(pricingTierRepository.findByTheaterIdAndSeatType(1L, SeatType.REGULAR))
                .thenReturn(List.of());

        BigDecimal result = pricingService.calculatePrice(testSeat, testShow);

        assertThat(result).isEqualByComparingTo("200.00");
    }

    @Test
    void calculatePrice_withAllDaysTier_shouldMatchAnyDay() {
        PricingTier tier = PricingTier.builder().id(1L).name("All Days")
                .theater(theater).seatType(SeatType.REGULAR)
                .basePrice(new BigDecimal("250.00")).multiplier(new BigDecimal("1.20"))
                .applicableDays("ALL").build();

        when(pricingTierRepository.findByTheaterIdAndSeatType(1L, SeatType.REGULAR))
                .thenReturn(List.of(tier));

        BigDecimal result = pricingService.calculatePrice(testSeat, testShow);

        // 250 * 1.20 = 300.00
        assertThat(result).isEqualByComparingTo("300.00");
    }
}
