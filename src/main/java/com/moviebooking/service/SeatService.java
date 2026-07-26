package com.moviebooking.service;

import com.moviebooking.entity.Screen;
import com.moviebooking.entity.Seat;
import com.moviebooking.enums.SeatType;
import com.moviebooking.exception.ResourceNotFoundException;
import com.moviebooking.repository.ScreenRepository;
import com.moviebooking.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;
    private final ScreenRepository screenRepository;

    @Transactional
    public List<Seat> bulkCreateSeats(Long screenId, List<SeatRequest> seats) {
        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found with id: " + screenId));
        List<Seat> createdSeats = new ArrayList<>();
        for (SeatRequest request : seats) {
            Seat seat = Seat.builder()
                    .screen(screen)
                    .row(request.row())
                    .number(request.number())
                    .seatType(request.seatType())
                    .build();
            createdSeats.add(seat);
        }
        log.info("Creating {} seats for screen id={}", seats.size(), screenId);
        return seatRepository.saveAll(createdSeats);
    }

    public List<Seat> getSeatsByScreen(Long screenId) {
        return seatRepository.findByScreenId(screenId);
    }

    @Transactional
    public Seat updateSeat(Long id, SeatType seatType) {
        Seat seat = seatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seat not found with id: " + id));
        seat.setSeatType(seatType);
        log.info("Updating seat id={} to type={}", id, seatType);
        return seatRepository.save(seat);
    }

    @Transactional
    public void deleteSeat(Long id) {
        Seat seat = seatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seat not found with id: " + id));
        log.info("Deleting seat id={}", id);
        seatRepository.delete(seat);
    }

    public record SeatRequest(String row, Integer number, SeatType seatType) {
    }
}
