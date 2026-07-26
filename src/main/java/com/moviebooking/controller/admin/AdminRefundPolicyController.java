package com.moviebooking.controller.admin;

import com.moviebooking.dto.request.CreateRefundPolicyRequest;
import com.moviebooking.dto.response.RefundPolicyResponse;
import com.moviebooking.entity.RefundPolicy;
import com.moviebooking.service.RefundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminRefundPolicyController {

    private final RefundService refundService;

    @PostMapping("/refund-policies")
    public ResponseEntity<RefundPolicyResponse> createRefundPolicy(@Valid @RequestBody CreateRefundPolicyRequest request) {
        RefundPolicy policy = refundService.createRefundPolicy(
                request.getTheaterId(), request.getName(),
                request.getHoursBeforeShow(), request.getRefundPercentage());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(policy));
    }

    @GetMapping("/refund-policies")
    public ResponseEntity<List<RefundPolicyResponse>> getAllRefundPolicies() {
        List<RefundPolicyResponse> policies = refundService.getRefundPolicies().stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(policies);
    }

    @PutMapping("/refund-policies/{id}")
    public ResponseEntity<RefundPolicyResponse> updateRefundPolicy(@PathVariable Long id,
                                                                    @Valid @RequestBody CreateRefundPolicyRequest request) {
        RefundPolicy policy = refundService.updateRefundPolicy(id, request.getName(),
                request.getHoursBeforeShow(), request.getRefundPercentage());
        return ResponseEntity.ok(mapToResponse(policy));
    }

    @DeleteMapping("/refund-policies/{id}")
    public ResponseEntity<Void> deleteRefundPolicy(@PathVariable Long id) {
        refundService.deleteRefundPolicy(id);
        return ResponseEntity.noContent().build();
    }

    private RefundPolicyResponse mapToResponse(RefundPolicy policy) {
        return RefundPolicyResponse.builder()
                .id(policy.getId())
                .name(policy.getName())
                .hoursBeforeShow(policy.getHoursBeforeShow())
                .refundPercentage(policy.getRefundPercentage())
                .theaterId(policy.getTheater().getId())
                .build();
    }
}
