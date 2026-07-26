package com.moviebooking.service;

import com.moviebooking.entity.Screen;
import com.moviebooking.entity.Theater;
import com.moviebooking.exception.ResourceNotFoundException;
import com.moviebooking.repository.ScreenRepository;
import com.moviebooking.repository.SeatRepository;
import com.moviebooking.repository.TheaterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScreenService {

    private final ScreenRepository screenRepository;
    private final TheaterRepository theaterRepository;
    private final SeatRepository seatRepository;

    @Transactional
    public Screen createScreen(Long theaterId, String name, Integer totalSeats) {
        Theater theater = theaterRepository.findById(theaterId)
                .orElseThrow(() -> new ResourceNotFoundException("Theater not found with id: " + theaterId));
        Screen screen = Screen.builder()
                .name(name)
                .theater(theater)
                .totalSeats(totalSeats)
                .build();
        log.info("Creating screen: {} in theater id={}", name, theaterId);
        return screenRepository.save(screen);
    }

    public List<Screen> getScreensByTheater(Long theaterId) {
        return screenRepository.findByTheaterId(theaterId);
    }

    @Transactional
    public Screen updateScreen(Long id, String name, Integer totalSeats) {
        Screen screen = screenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found with id: " + id));
        if (name != null) {
            screen.setName(name);
        }
        if (totalSeats != null) {
            screen.setTotalSeats(totalSeats);
        }
        log.info("Updating screen id={}", id);
        return screenRepository.save(screen);
    }

    @Transactional
    public void deleteScreen(Long id) {
        Screen screen = screenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found with id: " + id));
        if (!seatRepository.findByScreenId(id).isEmpty()) {
            throw new com.moviebooking.exception.DuplicateResourceException("Cannot delete screen with existing seats");
        }
        log.info("Deleting screen id={}", id);
        screenRepository.delete(screen);
    }
}
