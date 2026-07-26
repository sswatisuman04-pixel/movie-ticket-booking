package com.moviebooking.service;

import com.moviebooking.entity.Screen;
import com.moviebooking.entity.Seat;
import com.moviebooking.entity.Show;
import com.moviebooking.entity.ShowSeat;
import com.moviebooking.enums.ShowSeatStatus;
import com.moviebooking.exception.ResourceNotFoundException;
import com.moviebooking.repository.ScreenRepository;
import com.moviebooking.repository.SeatRepository;
import com.moviebooking.repository.ShowRepository;
import com.moviebooking.repository.ShowSeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShowService {

    private final ShowRepository showRepository;
    private final ScreenRepository screenRepository;
    private final SeatRepository seatRepository;
    private final ShowSeatRepository showSeatRepository;
    private final PricingService pricingService;

    @Transactional
    public Show createShow(Long screenId, String movieName, LocalTime startTime,
                           LocalTime endTime, LocalDate date) {
        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found with id: " + screenId));
        Show show = Show.builder()
                .screen(screen)
                .movieName(movieName)
                .startTime(startTime)
                .endTime(endTime)
                .date(date)
                .build();
        show = showRepository.save(show);
        log.info("Created show id={} for movie '{}' on screen id={}", show.getId(), movieName, screenId);
        generateShowSeats(show);
        return show;
    }

    public List<Show> getShowsByScreen(Long screenId) {
        return showRepository.findByScreenId(screenId);
    }

    public List<Show> searchShows(Long cityId, String movieName, LocalDate date) {
        List<Show> results;
        if (cityId != null && movieName != null) {
            results = showRepository.findByCityId(cityId);
            results = results.stream()
                    .filter(s -> s.getMovieName().toLowerCase().contains(movieName.toLowerCase()))
                    .toList();
        } else if (cityId != null) {
            results = showRepository.findByCityId(cityId);
        } else if (movieName != null) {
            results = showRepository.searchByMovieName(movieName);
        } else {
            results = showRepository.findAll();
        }
        if (date != null) {
            results = results.stream()
                    .filter(s -> s.getDate().equals(date))
                    .toList();
        }
        return results;
    }

    public Show getShowById(Long id) {
        return showRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found with id: " + id));
    }

    @Transactional
    public void deleteShow(Long id) {
        Show show = showRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found with id: " + id));
        List<ShowSeat> showSeats = showSeatRepository.findByShowId(id);
        showSeatRepository.deleteAll(showSeats);
        log.info("Deleting show id={}", id);
        showRepository.delete(show);
    }

    private void generateShowSeats(Show show) {
        List<Seat> seats = seatRepository.findByScreenId(show.getScreen().getId());
        List<ShowSeat> showSeats = new ArrayList<>();
        for (Seat seat : seats) {
            BigDecimal price = pricingService.calculatePrice(seat, show);
            ShowSeat showSeat = ShowSeat.builder()
                    .show(show)
                    .seat(seat)
                    .status(ShowSeatStatus.AVAILABLE)
                    .price(price)
                    .build();
            showSeats.add(showSeat);
        }
        showSeatRepository.saveAll(showSeats);
        log.info("Generated {} show seats for show id={}", showSeats.size(), show.getId());
    }
}
