package com.moviebooking.controller.admin;

import com.moviebooking.dto.request.CreateShowRequest;
import com.moviebooking.dto.response.ShowResponse;
import com.moviebooking.entity.Show;
import com.moviebooking.service.ShowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminShowController {

    private final ShowService showService;

    @PostMapping("/screens/{screenId}/shows")
    public ResponseEntity<ShowResponse> createShow(@PathVariable Long screenId,
                                                   @Valid @RequestBody CreateShowRequest request) {
        Show show = showService.createShow(screenId, request.getMovieName(),
                request.getStartTime(), request.getEndTime(), request.getDate());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(show));
    }

    @GetMapping("/shows")
    public ResponseEntity<List<ShowResponse>> getAllShows() {
        List<ShowResponse> shows = showService.searchShows(null, null, null).stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(shows);
    }

    @PutMapping("/shows/{id}")
    public ResponseEntity<ShowResponse> updateShow(@PathVariable Long id,
                                                   @Valid @RequestBody CreateShowRequest request) {
        // Delete and recreate as the service doesn't have an update method
        Show existing = showService.getShowById(id);
        showService.deleteShow(id);
        Show show = showService.createShow(existing.getScreen().getId(), request.getMovieName(),
                request.getStartTime(), request.getEndTime(), request.getDate());
        return ResponseEntity.ok(mapToResponse(show));
    }

    @DeleteMapping("/shows/{id}")
    public ResponseEntity<Void> deleteShow(@PathVariable Long id) {
        showService.deleteShow(id);
        return ResponseEntity.noContent().build();
    }

    private ShowResponse mapToResponse(Show show) {
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
}
