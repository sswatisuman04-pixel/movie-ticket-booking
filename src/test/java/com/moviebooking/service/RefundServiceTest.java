package com.moviebooking.service;

import com.moviebooking.entity.*;
import com.moviebooking.enums.BookingStatus;
import com.moviebooking.exception.RefundNotAllowedException;
import com.moviebooking.repository.RefundPolicyRepository;
import com.moviebooking.repository.TheaterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefundServiceTest {

    @Mock
    private RefundPolicyRepository refundPolicyRepository;
    @Mock
    private TheaterRepository theaterRepository;

    @InjectMocks
    private RefundService refundService;

    private Theater theater;
    private Booking testBooking;

    @BeforeEach
    void setUp() {
        City city = City.builder().id(1L).name("Mumbai").build();
        theater = Theater.builder().id(1L).name("PVR").address("123 St").city(city).build();
        Screen screen = Screen.builder().id(1L).name("Screen 1").theater(theater).totalSeats(100).build();

        // Show is 72 hours from now for flexible testing
        Show show = Show.builder().id(1L).movieName("Movie").screen(screen)
                .date(LocalDate.now().plusDays(3))
                .startTime(LocalTime.now())
                .endTime(LocalTime.now().plusHours(2))
                .build();

        testBooking = Booking.builder().id(1L).show(show)
                .status(BookingStatus.CONFIRMED)
                .totalAmount(new BigDecimal("500.00"))
                .bookingTime(LocalDateTime.now())
                .build();
    }

    @Test
    void calculateRefund_within48Hours_shouldReturn100Percent() {
        // Policy: if >= 48 hours before show → 100% refund
        RefundPolicy fullRefundPolicy = RefundPolicy.builder()
                .id(1L).name("Full Refund").theater(theater)
                .hoursBeforeShow(48).refundPercentage(100).build();

        RefundPolicy partialRefundPolicy = RefundPolicy.builder()
                .id(2L).name("Partial Refund").theater(theater)
                .hoursBeforeShow(12).refundPercentage(50).build();

        when(refundPolicyRepository.findByTheaterIdOrderByHoursBeforeShowDesc(1L))
                .thenReturn(List.of(fullRefundPolicy, partialRefundPolicy));

        BigDecimal refund = refundService.calculateRefund(testBooking);

        // 500 * 100 / 100 = 500
        assertThat(refund).isEqualByComparingTo("500.00");
    }

    @Test
    void calculateRefund_within12Hours_shouldReturnPartialRefund() {
        // Show is 10 hours away — use tomorrow at a fixed time to avoid midnight wrap
        LocalDateTime tenHoursFromNow = LocalDateTime.now().plusHours(10);
        Show soonShow = Show.builder().id(2L).movieName("Movie")
                .screen(testBooking.getShow().getScreen())
                .date(tenHoursFromNow.toLocalDate())
                .startTime(tenHoursFromNow.toLocalTime())
                .endTime(tenHoursFromNow.toLocalTime().plusHours(2))
                .build();
        Booking soonBooking = Booking.builder().id(2L).show(soonShow)
                .status(BookingStatus.CONFIRMED)
                .totalAmount(new BigDecimal("500.00"))
                .bookingTime(LocalDateTime.now())
                .build();

        // Policies: 48h=100%, 6h=50%
        RefundPolicy fullPolicy = RefundPolicy.builder()
                .id(1L).name("Full").theater(theater).hoursBeforeShow(48).refundPercentage(100).build();
        RefundPolicy partialPolicy = RefundPolicy.builder()
                .id(2L).name("Partial").theater(theater).hoursBeforeShow(6).refundPercentage(50).build();

        when(refundPolicyRepository.findByTheaterIdOrderByHoursBeforeShowDesc(1L))
                .thenReturn(List.of(fullPolicy, partialPolicy));

        BigDecimal refund = refundService.calculateRefund(soonBooking);

        // 10 hours until show. 48h policy: 48 > 10, doesn't apply.
        // 6h policy: 6 <= 10, applies → 500 * 50/100 = 250
        assertThat(refund).isEqualByComparingTo("250.00");
    }

    @Test
    void calculateRefund_noMatchingPolicy_shouldThrowRefundNotAllowed() {
        // Show is 2 hours away
        LocalDateTime twoHoursFromNow = LocalDateTime.now().plusHours(2);
        Show imminentShow = Show.builder().id(3L).movieName("Movie")
                .screen(testBooking.getShow().getScreen())
                .date(twoHoursFromNow.toLocalDate())
                .startTime(twoHoursFromNow.toLocalTime())
                .endTime(twoHoursFromNow.toLocalTime().plusHours(2))
                .build();
        Booking imminentBooking = Booking.builder().id(3L).show(imminentShow)
                .status(BookingStatus.CONFIRMED)
                .totalAmount(new BigDecimal("500.00"))
                .bookingTime(LocalDateTime.now())
                .build();

        // Only policy requires 6 hours before show
        RefundPolicy policy = RefundPolicy.builder()
                .id(1L).name("Min 6h").theater(theater).hoursBeforeShow(6).refundPercentage(50).build();

        when(refundPolicyRepository.findByTheaterIdOrderByHoursBeforeShowDesc(1L))
                .thenReturn(List.of(policy));

        assertThatThrownBy(() -> refundService.calculateRefund(imminentBooking))
                .isInstanceOf(RefundNotAllowedException.class)
                .hasMessageContaining("No refund policy applicable");
    }
}
