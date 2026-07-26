package com.moviebooking.service;

import com.moviebooking.entity.*;
import com.moviebooking.enums.*;
import com.moviebooking.exception.*;
import com.moviebooking.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private ShowSeatRepository showSeatRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private BookingSeatRepository bookingSeatRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ShowRepository showRepository;
    @Mock
    private DiscountService discountService;
    @Mock
    private RefundService refundService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private BookingService bookingService;

    private User testUser;
    private Show testShow;
    private ShowSeat testShowSeat;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(bookingService, "holdDurationMinutes", 5);

        testUser = User.builder().id(1L).name("Test User").email("test@test.com")
                .password("encoded").role(Role.CUSTOMER).build();

        City city = City.builder().id(1L).name("Mumbai").build();
        Theater theater = Theater.builder().id(1L).name("PVR").address("123 Main St").city(city).build();
        Screen screen = Screen.builder().id(1L).name("Screen 1").theater(theater).totalSeats(100).build();

        testShow = Show.builder().id(1L).movieName("Test Movie").screen(screen)
                .date(LocalDate.now().plusDays(3)).startTime(LocalTime.of(14, 0)).endTime(LocalTime.of(16, 0)).build();

        Seat seat = Seat.builder().id(1L).screen(screen).row("A").number(1).seatType(SeatType.REGULAR).build();
        testShowSeat = ShowSeat.builder().id(1L).show(testShow).seat(seat)
                .status(ShowSeatStatus.AVAILABLE).price(new BigDecimal("300.00")).version(0).build();
    }

    @Test
    void holdSeats_whenAllAvailable_shouldSetStatusToHeld() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(showRepository.findById(1L)).thenReturn(Optional.of(testShow));
        when(showSeatRepository.findById(1L)).thenReturn(Optional.of(testShowSeat));
        when(showSeatRepository.save(any(ShowSeat.class))).thenReturn(testShowSeat);

        BookingService.HoldResponse result = bookingService.holdSeats(1L, 1L, List.of(1L));

        assertThat(result.showSeatIds()).containsExactly(1L);
        assertThat(result.holdExpiresAt()).isAfter(LocalDateTime.now());
        verify(showSeatRepository).save(argThat(seat -> seat.getStatus() == ShowSeatStatus.HELD));
    }

    @Test
    void holdSeats_whenSeatAlreadyHeld_shouldThrowSeatUnavailableException() {
        testShowSeat.setStatus(ShowSeatStatus.HELD);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(showRepository.findById(1L)).thenReturn(Optional.of(testShow));
        when(showSeatRepository.findById(1L)).thenReturn(Optional.of(testShowSeat));

        assertThatThrownBy(() -> bookingService.holdSeats(1L, 1L, List.of(1L)))
                .isInstanceOf(SeatUnavailableException.class)
                .hasMessageContaining("not available");
    }

    @Test
    void confirmBooking_whenHoldsValid_shouldCreateBookingAndPayment() {
        testShowSeat.setStatus(ShowSeatStatus.HELD);
        testShowSeat.setHeldBy(testUser);
        testShowSeat.setHoldExpiresAt(LocalDateTime.now().plusMinutes(5));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(showRepository.findById(1L)).thenReturn(Optional.of(testShow));
        when(showSeatRepository.findById(1L)).thenReturn(Optional.of(testShowSeat));
        when(showSeatRepository.save(any(ShowSeat.class))).thenReturn(testShowSeat);

        Booking savedBooking = Booking.builder().id(1L).user(testUser).show(testShow)
                .status(BookingStatus.CONFIRMED).totalAmount(new BigDecimal("300.00"))
                .discountApplied(BigDecimal.ZERO).bookingTime(LocalDateTime.now()).build();
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);
        when(paymentRepository.save(any(Payment.class))).thenReturn(Payment.builder().id(1L).build());
        when(bookingSeatRepository.save(any(BookingSeat.class))).thenReturn(BookingSeat.builder().build());

        Booking result = bookingService.confirmBooking(1L, 1L, List.of(1L), null);

        assertThat(result.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(result.getTotalAmount()).isEqualByComparingTo("300.00");
        verify(paymentRepository).save(argThat(p -> p.getStatus() == PaymentStatus.SUCCESS));
        verify(eventPublisher).publishEvent(any(Object.class));
    }

    @Test
    void confirmBooking_whenHoldExpired_shouldThrowHoldExpiredException() {
        testShowSeat.setStatus(ShowSeatStatus.HELD);
        testShowSeat.setHeldBy(testUser);
        testShowSeat.setHoldExpiresAt(LocalDateTime.now().minusMinutes(1));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(showRepository.findById(1L)).thenReturn(Optional.of(testShow));
        when(showSeatRepository.findById(1L)).thenReturn(Optional.of(testShowSeat));

        assertThatThrownBy(() -> bookingService.confirmBooking(1L, 1L, List.of(1L), null))
                .isInstanceOf(HoldExpiredException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void confirmBooking_withDiscountCode_shouldApplyDiscount() {
        testShowSeat.setStatus(ShowSeatStatus.HELD);
        testShowSeat.setHeldBy(testUser);
        testShowSeat.setHoldExpiresAt(LocalDateTime.now().plusMinutes(5));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(showRepository.findById(1L)).thenReturn(Optional.of(testShow));
        when(showSeatRepository.findById(1L)).thenReturn(Optional.of(testShowSeat));
        when(showSeatRepository.save(any(ShowSeat.class))).thenReturn(testShowSeat);
        when(discountService.validateAndApplyDiscount("SAVE10", new BigDecimal("300.00")))
                .thenReturn(new BigDecimal("30.00"));
        when(discountService.findByCode("SAVE10")).thenReturn(
                DiscountCode.builder().id(1L).code("SAVE10").type(DiscountType.PERCENTAGE)
                        .value(BigDecimal.TEN).build());

        Booking savedBooking = Booking.builder().id(1L).user(testUser).show(testShow)
                .status(BookingStatus.CONFIRMED).totalAmount(new BigDecimal("270.00"))
                .discountApplied(new BigDecimal("30.00")).bookingTime(LocalDateTime.now()).build();
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);
        when(paymentRepository.save(any(Payment.class))).thenReturn(Payment.builder().id(1L).build());
        when(bookingSeatRepository.save(any(BookingSeat.class))).thenReturn(BookingSeat.builder().build());

        Booking result = bookingService.confirmBooking(1L, 1L, List.of(1L), "SAVE10");

        assertThat(result.getDiscountApplied()).isEqualByComparingTo("30.00");
        assertThat(result.getTotalAmount()).isEqualByComparingTo("270.00");
        verify(discountService).validateAndApplyDiscount("SAVE10", new BigDecimal("300.00"));
    }

    @Test
    void cancelBooking_whenConfirmed_shouldRefundAndReleaseSeats() {
        Booking booking = Booking.builder().id(1L).user(testUser).show(testShow)
                .status(BookingStatus.CONFIRMED).totalAmount(new BigDecimal("300.00"))
                .discountApplied(BigDecimal.ZERO).bookingTime(LocalDateTime.now()).build();

        Payment payment = Payment.builder().id(1L).booking(booking)
                .amount(new BigDecimal("300.00")).status(PaymentStatus.SUCCESS)
                .paymentTime(LocalDateTime.now()).build();

        BookingSeat bookingSeat = BookingSeat.builder()
                .id(new BookingSeatId(1L, 1L)).booking(booking).showSeat(testShowSeat).build();

        when(bookingRepository.findByUserIdAndId(1L, 1L)).thenReturn(Optional.of(booking));
        when(refundService.calculateRefund(booking)).thenReturn(new BigDecimal("300.00"));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(paymentRepository.findByBookingId(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(bookingSeatRepository.findByBookingId(1L)).thenReturn(List.of(bookingSeat));
        when(showSeatRepository.save(any(ShowSeat.class))).thenReturn(testShowSeat);

        BookingService.CancellationResponse result = bookingService.cancelBooking(1L, 1L);

        assertThat(result.refundAmount()).isEqualByComparingTo("300.00");
        assertThat(result.status()).isEqualTo(BookingStatus.CANCELLED);
        verify(showSeatRepository).save(argThat(s -> s.getStatus() == ShowSeatStatus.AVAILABLE));
        verify(eventPublisher).publishEvent(any(Object.class));
    }

    @Test
    void cancelBooking_whenAlreadyCancelled_shouldThrowException() {
        Booking booking = Booking.builder().id(1L).user(testUser).show(testShow)
                .status(BookingStatus.CANCELLED).totalAmount(new BigDecimal("300.00"))
                .bookingTime(LocalDateTime.now()).build();

        when(bookingRepository.findByUserIdAndId(1L, 1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancelBooking(1L, 1L))
                .isInstanceOf(RefundNotAllowedException.class)
                .hasMessageContaining("not in CONFIRMED status");
    }
}
