package com.moviebooking.controller.admin;

import com.moviebooking.dto.request.CreateTheaterRequest;
import com.moviebooking.dto.response.TheaterResponse;
import com.moviebooking.entity.Theater;
import com.moviebooking.service.TheaterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminTheaterController {

    private final TheaterService theaterService;

    @PostMapping("/cities/{cityId}/theaters")
    public ResponseEntity<TheaterResponse> createTheater(@PathVariable Long cityId,
                                                         @Valid @RequestBody CreateTheaterRequest request) {
        Theater theater = theaterService.createTheater(cityId, request.getName(), request.getAddress());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(theater));
    }

    @GetMapping("/cities/{cityId}/theaters")
    public ResponseEntity<List<TheaterResponse>> getTheatersByCity(@PathVariable Long cityId) {
        List<TheaterResponse> theaters = theaterService.getTheatersByCity(cityId).stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(theaters);
    }

    @PutMapping("/theaters/{id}")
    public ResponseEntity<TheaterResponse> updateTheater(@PathVariable Long id,
                                                         @Valid @RequestBody CreateTheaterRequest request) {
        Theater theater = theaterService.updateTheater(id, request.getName(), request.getAddress());
        return ResponseEntity.ok(mapToResponse(theater));
    }

    @DeleteMapping("/theaters/{id}")
    public ResponseEntity<Void> deleteTheater(@PathVariable Long id) {
        theaterService.deleteTheater(id);
        return ResponseEntity.noContent().build();
    }

    private TheaterResponse mapToResponse(Theater theater) {
        return TheaterResponse.builder()
                .id(theater.getId())
                .name(theater.getName())
                .address(theater.getAddress())
                .cityId(theater.getCity().getId())
                .cityName(theater.getCity().getName())
                .build();
    }
}
