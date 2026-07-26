package com.moviebooking.controller.admin;

import com.moviebooking.dto.request.CreateCityRequest;
import com.moviebooking.dto.response.CityResponse;
import com.moviebooking.entity.City;
import com.moviebooking.service.CityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminCityController {

    private final CityService cityService;

    @PostMapping("/cities")
    public ResponseEntity<CityResponse> createCity(@Valid @RequestBody CreateCityRequest request) {
        City city = cityService.createCity(request.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(city));
    }

    @GetMapping("/cities")
    public ResponseEntity<List<CityResponse>> getAllCities() {
        List<CityResponse> cities = cityService.getAllCities().stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(cities);
    }

    @PutMapping("/cities/{id}")
    public ResponseEntity<CityResponse> updateCity(@PathVariable Long id,
                                                   @Valid @RequestBody CreateCityRequest request) {
        City city = cityService.updateCity(id, request.getName());
        return ResponseEntity.ok(mapToResponse(city));
    }

    @DeleteMapping("/cities/{id}")
    public ResponseEntity<Void> deleteCity(@PathVariable Long id) {
        cityService.deleteCity(id);
        return ResponseEntity.noContent().build();
    }

    private CityResponse mapToResponse(City city) {
        return CityResponse.builder()
                .id(city.getId())
                .name(city.getName())
                .build();
    }
}
