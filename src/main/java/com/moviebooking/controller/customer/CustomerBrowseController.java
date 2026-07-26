package com.moviebooking.controller.customer;

import com.moviebooking.dto.response.*;
import com.moviebooking.entity.City;
import com.moviebooking.entity.Show;
import com.moviebooking.entity.ShowSeat;
import com.moviebooking.entity.Theater;
import com.moviebooking.repository.ShowSeatRepository;
import com.moviebooking.service.CityService;
import com.moviebooking.service.ShowService;
import com.moviebooking.service.TheaterService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CustomerBrowseController {

    private final CityService cityService;
    private final TheaterService theaterService;
    private final ShowService showService;
    private final ShowSeatRepository showSeatRepository;

    @GetMapping("/cities")
    public ResponseEntity<List<CityResponse>> getAllCities() {
        List<CityResponse> cities = cityService.getAllCities().stream()
                .map(this::mapCityToResponse)
                .toList();
        return ResponseEntity.ok(cities);
    }

    @GetMapping("/cities/{cityId}/theaters")
    public ResponseEntity<List<TheaterResponse>> getTheatersByCity(@PathVariable Long cityId) {
        List<TheaterResponse> theaters = theaterService.getTheatersByCity(cityId).stream()
                .map(this::mapTheaterToResponse)
                .toList();
        return ResponseEntity.ok(theaters);
    }

    @GetMapping("/theaters/{theaterId}/shows")
    public ResponseEntity<List<ShowResponse>> getShowsByTheater(@PathVariable Long theaterId) {
        // Get shows via screen → theater relationship
        List<ShowResponse> shows = showService.searchShows(null, null, null).stream()
                .filter(s -> s.getScreen().getTheater().getId().equals(theaterId))
                .map(this::mapShowToResponse)
                .toList();
        return ResponseEntity.ok(shows);
    }

    @GetMapping("/shows")
    public ResponseEntity<List<ShowResponse>> searchShows(
            @RequestParam(required = false) Long cityId,
            @RequestParam(required = false) String movieName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<ShowResponse> shows = showService.searchShows(cityId, movieName, date).stream()
                .map(this::mapShowToResponse)
                .toList();
        return ResponseEntity.ok(shows);
    }

    @GetMapping("/shows/{showId}/seats")
    public ResponseEntity<List<ShowSeatResponse>> getShowSeats(@PathVariable Long showId) {
        List<ShowSeatResponse> seats = showSeatRepository.findByShowId(showId).stream()
                .map(this::mapShowSeatToResponse)
                .toList();
        return ResponseEntity.ok(seats);
    }

    private CityResponse mapCityToResponse(City city) {
        return CityResponse.builder()
                .id(city.getId())
                .name(city.getName())
                .build();
    }

    private TheaterResponse mapTheaterToResponse(Theater theater) {
        return TheaterResponse.builder()
                .id(theater.getId())
                .name(theater.getName())
                .address(theater.getAddress())
                .cityId(theater.getCity().getId())
                .cityName(theater.getCity().getName())
                .build();
    }

    private ShowResponse mapShowToResponse(Show show) {
        return ShowResponse.builder()
                .id(show.getId())
                .movieName(show.getMovieName())
                .screenId(show.getScreen().getId())
                .screenName(show.getScreen().getName())
                .theaterName(show.getScreen().getTheater().getName())
                .cityName(show.getScreen().getTheater().getCity().getName())
                .startTime(show.getStartTime())
                .endTime(show.getEndTime())
                .date(show.getDate())
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
