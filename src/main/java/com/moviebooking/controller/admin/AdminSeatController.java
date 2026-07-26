package com.moviebooking.controller.admin;

import com.moviebooking.dto.request.BulkCreateSeatsRequest;
import com.moviebooking.dto.request.CreateSeatRequest;
import com.moviebooking.dto.response.SeatResponse;
import com.moviebooking.entity.Seat;
import com.moviebooking.service.SeatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminSeatController {

    private final SeatService seatService;

    @PostMapping("/screens/{screenId}/seats")
    public ResponseEntity<List<SeatResponse>> bulkCreateSeats(@PathVariable Long screenId,
                                                               @Valid @RequestBody BulkCreateSeatsRequest request) {
        List<SeatService.SeatRequest> seatRequests = request.getSeats().stream()
                .map(s -> new SeatService.SeatRequest(s.getRow(), s.getNumber(), s.getSeatType()))
                .toList();
        List<SeatResponse> seats = seatService.bulkCreateSeats(screenId, seatRequests).stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.status(HttpStatus.CREATED).body(seats);
    }

    @GetMapping("/screens/{screenId}/seats")
    public ResponseEntity<List<SeatResponse>> getSeatsByScreen(@PathVariable Long screenId) {
        List<SeatResponse> seats = seatService.getSeatsByScreen(screenId).stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(seats);
    }

    @PutMapping("/seats/{id}")
    public ResponseEntity<SeatResponse> updateSeat(@PathVariable Long id,
                                                   @Valid @RequestBody CreateSeatRequest request) {
        Seat seat = seatService.updateSeat(id, request.getSeatType());
        return ResponseEntity.ok(mapToResponse(seat));
    }

    @DeleteMapping("/seats/{id}")
    public ResponseEntity<Void> deleteSeat(@PathVariable Long id) {
        seatService.deleteSeat(id);
        return ResponseEntity.noContent().build();
    }

    private SeatResponse mapToResponse(Seat seat) {
        return SeatResponse.builder()
                .id(seat.getId())
                .row(seat.getRow())
                .number(seat.getNumber())
                .seatType(seat.getSeatType())
                .build();
    }
}
