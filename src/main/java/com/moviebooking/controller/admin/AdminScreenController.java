package com.moviebooking.controller.admin;

import com.moviebooking.dto.request.CreateScreenRequest;
import com.moviebooking.dto.response.ScreenResponse;
import com.moviebooking.entity.Screen;
import com.moviebooking.service.ScreenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminScreenController {

    private final ScreenService screenService;

    @PostMapping("/theaters/{theaterId}/screens")
    public ResponseEntity<ScreenResponse> createScreen(@PathVariable Long theaterId,
                                                       @Valid @RequestBody CreateScreenRequest request) {
        Screen screen = screenService.createScreen(theaterId, request.getName(), request.getTotalSeats());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(screen));
    }

    @GetMapping("/theaters/{theaterId}/screens")
    public ResponseEntity<List<ScreenResponse>> getScreensByTheater(@PathVariable Long theaterId) {
        List<ScreenResponse> screens = screenService.getScreensByTheater(theaterId).stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(screens);
    }

    @PutMapping("/screens/{id}")
    public ResponseEntity<ScreenResponse> updateScreen(@PathVariable Long id,
                                                       @Valid @RequestBody CreateScreenRequest request) {
        Screen screen = screenService.updateScreen(id, request.getName(), request.getTotalSeats());
        return ResponseEntity.ok(mapToResponse(screen));
    }

    @DeleteMapping("/screens/{id}")
    public ResponseEntity<Void> deleteScreen(@PathVariable Long id) {
        screenService.deleteScreen(id);
        return ResponseEntity.noContent().build();
    }

    private ScreenResponse mapToResponse(Screen screen) {
        return ScreenResponse.builder()
                .id(screen.getId())
                .name(screen.getName())
                .theaterId(screen.getTheater().getId())
                .theaterName(screen.getTheater().getName())
                .totalSeats(screen.getTotalSeats())
                .build();
    }
}
