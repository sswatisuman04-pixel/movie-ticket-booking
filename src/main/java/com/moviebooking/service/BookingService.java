package com.moviebooking.service;

import com.moviebooking.entity.*;
import com.moviebooking.enums.BookingStatus;
import com.moviebooking.enums.PaymentStatus;
import com.moviebooking.enums.ShowSeatStatus;
import com.moviebooking.event.BookingCancelledEvent;
import com.moviebooking.event.BookingConfirmedEvent;
import com.moviebooking.exception.*;
import com.moviebooking.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final ShowSeatRepository showSeatRepository;
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final ShowRepository showRepository;
    private final DiscountService discountService;
    private final RefundService refundService;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${app.booking.hold-duration-minutes}")
    private int holdDurationMinutes;

    @Transactional
    public HoldResponse holdSeats(Long userId, Long showId, List<Long> showSeatIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        showRepository.findById(showId)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found with id: " + showId));

        List<ShowSeat> seats = showSeatIds.stream()
                .map(id -> showSeatRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("ShowSeat not found with id: " + id)))
                .toList();

        // Validate all seats are AVAILABLE
        for (ShowSeat seat : seats) {
            if (seat.getStatus() != ShowSeatStatus.AVAILABLE) {
                throw new SeatUnavailableException("Seat id=" + seat.getId() + " is not available");
            }
        }

        LocalDateTime holdExpiresAt = LocalDateTime.now().plusMinutes(holdDurationMinutes);

        try {
            for (ShowSeat seat : seats) {
                seat.setStatus(ShowSeatStatus.HELD);
                seat.setHeldBy(user);
                seat.setHoldExpiresAt(holdExpiresAt);
                showSeatRepository.save(seat);
            }
        } catch (OptimisticLockingFailureException ex) {
            throw new SeatUnavailableException("One or more seats were taken by another user. Please try again.");
        }

        log.info("User id={} held {} seats for show id={}, expires at {}", userId, showSeatIds.size(), showId, holdExpiresAt);
        return new HoldResponse(showSeatIds, holdExpiresAt);
    }

    @Transactional
    public Booking confirmBooking(Long userId, Long showId, List<Long> showSeatIds, String discountCodeStr) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found with id: " + showId));

        // Validate seats are HELD by this user and not expired
        List<ShowSeat> heldSeats = showSeatIds.stream()
                .map(id -> showSeatRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("ShowSeat not found with id: " + id)))
                .toList();

        for (ShowSeat seat : heldSeats) {
            if (seat.getStatus() != ShowSeatStatus.HELD) {
                throw new SeatUnavailableException("Seat id=" + seat.getId() + " is not in HELD status");
            }
            if (seat.getHeldBy() == null || !seat.getHeldBy().getId().equals(userId)) {
                throw new SeatUnavailableException("Seat id=" + seat.getId() + " is not held by this user");
            }
            if (seat.getHoldExpiresAt() != null && seat.getHoldExpiresAt().isBefore(LocalDateTime.now())) {
                throw new HoldExpiredException("Hold for seat id=" + seat.getId() + " has expired");
            }
        }

        // Calculate total price
        BigDecimal totalAmount = heldSeats.stream()
                .map(ShowSeat::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Apply discount if code provided
        BigDecimal discountApplied = BigDecimal.ZERO;
        DiscountCode discountCode = null;
        if (discountCodeStr != null && !discountCodeStr.isBlank()) {
            discountApplied = discountService.validateAndApplyDiscount(discountCodeStr, totalAmount);
            discountCode = new DiscountCode();
            discountCode.setCode(discountCodeStr);
        }

        BigDecimal finalAmount = totalAmount.subtract(discountApplied);

        // Create Booking
        Booking booking = Booking.builder()
                .user(user)
                .show(show)
                .status(BookingStatus.CONFIRMED)
                .totalAmount(finalAmount)
                .discountApplied(discountApplied)
                .discountCode(discountCode)
                .bookingTime(LocalDateTime.now())
                .build();
        booking = bookingRepository.save(booking);

        // Create Payment
        Payment payment = Payment.builder()
                .booking(booking)
                .amount(finalAmount)
                .status(PaymentStatus.SUCCESS)
                .paymentTime(LocalDateTime.now())
                .build();
        paymentRepository.save(payment);

        // Update seats to BOOKED and create BookingSeats
        for (ShowSeat seat : heldSeats) {
            seat.setStatus(ShowSeatStatus.BOOKED);
            seat.setHeldBy(null);
            seat.setHoldExpiresAt(null);
            showSeatRepository.save(seat);

            BookingSeat bookingSeat = BookingSeat.builder()
                    .id(new BookingSeatId(booking.getId(), seat.getId()))
                    .booking(booking)
                    .showSeat(seat)
                    .build();
            bookingSeatRepository.save(bookingSeat);
        }

        // Publish event
        eventPublisher.publishEvent(new BookingConfirmedEvent(booking.getId(), userId, showId));

        log.info("Booking confirmed: id={}, user={}, show={}, amount={}", booking.getId(), userId, showId, finalAmount);
        return booking;
    }

    @Transactional
    public CancellationResponse cancelBooking(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findByUserIdAndId(userId, bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId + " for user: " + userId));

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new RefundNotAllowedException("Booking is not in CONFIRMED status");
        }

        // Calculate refund
        BigDecimal refundAmount = refundService.calculateRefund(booking);

        // Update booking status
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        // Update payment with refund
        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for booking id: " + bookingId));
        payment.setRefundAmount(refundAmount);
        payment.setRefundTime(LocalDateTime.now());
        payment.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(payment);

        // Release seats back to AVAILABLE
        List<BookingSeat> bookingSeats = bookingSeatRepository.findByBookingId(bookingId);
        for (BookingSeat bookingSeat : bookingSeats) {
            ShowSeat showSeat = bookingSeat.getShowSeat();
            showSeat.setStatus(ShowSeatStatus.AVAILABLE);
            showSeatRepository.save(showSeat);
        }

        // Publish event
        eventPublisher.publishEvent(new BookingCancelledEvent(bookingId, userId, booking.getShow().getId()));

        log.info("Booking cancelled: id={}, user={}, refund={}", bookingId, userId, refundAmount);
        return new CancellationResponse(bookingId, refundAmount, booking.getStatus());
    }

    public List<Booking> getBookingsByUser(Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    public Booking getBookingById(Long userId, Long bookingId) {
        return bookingRepository.findByUserIdAndId(userId, bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId + " for user: " + userId));
    }

    public record HoldResponse(List<Long> showSeatIds, LocalDateTime holdExpiresAt) {
    }

    public record CancellationResponse(Long bookingId, BigDecimal refundAmount, BookingStatus status) {
    }
}
