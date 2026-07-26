package com.moviebooking.controller.admin;

import com.moviebooking.dto.request.CreateDiscountCodeRequest;
import com.moviebooking.dto.response.DiscountCodeResponse;
import com.moviebooking.entity.DiscountCode;
import com.moviebooking.service.DiscountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminDiscountCodeController {

    private final DiscountService discountService;

    @PostMapping("/discount-codes")
    public ResponseEntity<DiscountCodeResponse> createDiscountCode(@Valid @RequestBody CreateDiscountCodeRequest request) {
        DiscountCode code = discountService.createDiscountCode(
                request.getCode(), request.getType(), request.getValue(),
                request.getMaxUses(), request.getValidFrom(), request.getValidTo(),
                request.getMinBookingAmount());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(code));
    }

    @GetMapping("/discount-codes")
    public ResponseEntity<List<DiscountCodeResponse>> getAllDiscountCodes() {
        List<DiscountCodeResponse> codes = discountService.getDiscountCodes().stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(codes);
    }

    @PutMapping("/discount-codes/{id}")
    public ResponseEntity<DiscountCodeResponse> updateDiscountCode(@PathVariable Long id,
                                                                    @Valid @RequestBody CreateDiscountCodeRequest request) {
        DiscountCode code = discountService.updateDiscountCode(id,
                request.getCode(), request.getType(), request.getValue(),
                request.getMaxUses(), request.getValidFrom(), request.getValidTo(),
                request.getMinBookingAmount());
        return ResponseEntity.ok(mapToResponse(code));
    }

    @DeleteMapping("/discount-codes/{id}")
    public ResponseEntity<Void> deleteDiscountCode(@PathVariable Long id) {
        discountService.deleteDiscountCode(id);
        return ResponseEntity.noContent().build();
    }

    private DiscountCodeResponse mapToResponse(DiscountCode code) {
        return DiscountCodeResponse.builder()
                .id(code.getId())
                .code(code.getCode())
                .type(code.getType())
                .value(code.getValue())
                .maxUses(code.getMaxUses())
                .currentUses(code.getCurrentUses())
                .validFrom(code.getValidFrom())
                .validTo(code.getValidTo())
                .minBookingAmount(code.getMinBookingAmount())
                .build();
    }
}
