package com.moviebooking.controller.admin;

import com.moviebooking.dto.request.CreatePricingTierRequest;
import com.moviebooking.dto.response.PricingTierResponse;
import com.moviebooking.entity.PricingTier;
import com.moviebooking.service.PricingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminPricingTierController {

    private final PricingService pricingService;

    @PostMapping("/pricing-tiers")
    public ResponseEntity<PricingTierResponse> createPricingTier(@Valid @RequestBody CreatePricingTierRequest request) {
        PricingTier tier = pricingService.createPricingTier(
                request.getTheaterId(), request.getName(), request.getSeatType(),
                request.getBasePrice(), request.getMultiplier(), request.getApplicableDays());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(tier));
    }

    @GetMapping("/pricing-tiers")
    public ResponseEntity<List<PricingTierResponse>> getAllPricingTiers() {
        List<PricingTierResponse> tiers = pricingService.getPricingTiers().stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(tiers);
    }

    @PutMapping("/pricing-tiers/{id}")
    public ResponseEntity<PricingTierResponse> updatePricingTier(@PathVariable Long id,
                                                                  @Valid @RequestBody CreatePricingTierRequest request) {
        PricingTier tier = pricingService.updatePricingTier(id, request.getName(), request.getSeatType(),
                request.getBasePrice(), request.getMultiplier(), request.getApplicableDays());
        return ResponseEntity.ok(mapToResponse(tier));
    }

    @DeleteMapping("/pricing-tiers/{id}")
    public ResponseEntity<Void> deletePricingTier(@PathVariable Long id) {
        pricingService.deletePricingTier(id);
        return ResponseEntity.noContent().build();
    }

    private PricingTierResponse mapToResponse(PricingTier tier) {
        return PricingTierResponse.builder()
                .id(tier.getId())
                .name(tier.getName())
                .seatType(tier.getSeatType())
                .multiplier(tier.getMultiplier())
                .basePrice(tier.getBasePrice())
                .applicableDays(tier.getApplicableDays())
                .theaterId(tier.getTheater().getId())
                .build();
    }
}
