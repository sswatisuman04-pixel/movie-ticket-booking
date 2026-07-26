package com.moviebooking.controller.customer;

import com.moviebooking.dto.request.ConfirmBookingRequest;
import com.moviebooking.dto.request.HoldSeatsRequest;
import com.moviebooking.dto.response.*;
import com.moviebooking.entity.Booking;
import com.moviebooking.entity.BookingSeat;
import com.moviebooking.entity.ShowSeat;
import com.moviebooking.repository.BookingSeatRepository;
import com.moviebooking.repository.ShowSeatRepository;
import com.moviebooking.security.CustomUserDetails;
import com.moviebooking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class CustomerBookingController {

    private final BookingService bookingService;
    private final ShowSeatRepository showSeatRepository;
    private final BookingSeatRepository bookingSeatRepository;

    private Long getCurrentUserId() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return userDetails.getId();
    }

    @PostMapping("/hold")
    public ResponseEntity<HoldResponse> holdSeats(@Valid @RequestBody HoldSeatsRequest request) {
        Long userId = getCurrentUserId();
        BookingService.HoldResponse holdResult = bookingService.holdSeats(userId, request.getShowId(), request.getShowSeatIds());

        List<ShowSeatResponse> heldSeats = holdResult.showSeatIds().stream()
                .map(id -> showSeatRepository.findById(id).orElse(null))
                .filter(s -> s != null)
                .map(this::mapShowSeatToResponse)
                .toList();

        HoldResponse response = HoldResponse.builder()
                .showId(request.getShowId())
                .heldSeats(heldSeats)
                .holdExpiresAt(holdResult.holdExpiresAt())
                .message("Seats held successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/confirm")
    public ResponseEntity<BookingResponse> confirmBooking(@Valid @RequestBody ConfirmBookingRequest request) {
        Long userId = getCurrentUserId();
        Booking booking = bookingService.confirmBooking(userId, request.getShowId(),
                request.getShowSeatIds(), request.getDiscountCode());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapBookingToResponse(booking));
    }

    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<CancellationResponse> cancelBooking(@PathVariable Long bookingId) {
        Long userId = getCurrentUserId();
        BookingService.CancellationResponse result = bookingService.cancelBooking(userId, bookingId);

        CancellationResponse response = CancellationResponse.builder()
                .bookingId(result.bookingId())
                .refundAmount(result.refundAmount())
                .message("Booking cancelled successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<BookingResponse>> getUserBookings() {
        Long userId = getCurrentUserId();
        List<BookingResponse> bookings = bookingService.getBookingsByUser(userId).stream()
                .map(this::mapBookingToResponse)
                .toList();
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponse> getBookingDetail(@PathVariable Long bookingId) {
        Long userId = getCurrentUserId();
        Booking booking = bookingService.getBookingById(userId, bookingId);
        return ResponseEntity.ok(mapBookingToResponse(booking));
    }

    private BookingResponse mapBookingToResponse(Booking booking) {
        List<BookingSeat> bookingSeats = bookingSeatRepository.findByBookingId(booking.getId());
        List<SeatResponse> seats = bookingSeats.stream()
                .map(bs -> {
                    ShowSeat showSeat = bs.getShowSeat();
                    return SeatResponse.builder()
                            .id(showSeat.getSeat().getId())
                            .row(showSeat.getSeat().getRow())
                            .number(showSeat.getSeat().getNumber())
                            .seatType(showSeat.getSeat().getSeatType())
                            .build();
                })
                .toList();

        return BookingResponse.builder()
                .id(booking.getId())
                .showId(booking.getShow().getId())
                .movieName(booking.getShow().getMovieName())
                .theaterName(booking.getShow().getScreen().getTheater().getName())
                .date(booking.getShow().getDate())
                .startTime(booking.getShow().getStartTime())
                .status(booking.getStatus())
                .totalAmount(booking.getTotalAmount())
                .discountApplied(booking.getDiscountApplied())
                .seats(seats)
                .bookingTime(booking.getBookingTime())
                .build();
    }

    private ShowSeatResponse mapShowSeatToResponse(ShowSeat showSeat) {
        return ShowSeatResponse.builder()
                .id(showSeat.getId())
                .seatId(showSeat.getSeat().getId())
                .row(showSeat.getSeat().getRow())
                .number(showSeat.getSeat().getNumber())
                .seatType(showSeat.getSeat().getSeatType())
                .status(showSeat.getStatus())
                .price(showSeat.getPrice())
                .build();
    }
}
