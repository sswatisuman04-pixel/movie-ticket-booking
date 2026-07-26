package com.moviebooking.service;

import com.moviebooking.entity.Booking;
import com.moviebooking.entity.RefundPolicy;
import com.moviebooking.entity.Theater;
import com.moviebooking.exception.RefundNotAllowedException;
import com.moviebooking.exception.ResourceNotFoundException;
import com.moviebooking.repository.RefundPolicyRepository;
import com.moviebooking.repository.TheaterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefundService {

    private final RefundPolicyRepository refundPolicyRepository;
    private final TheaterRepository theaterRepository;

    @Transactional
    public RefundPolicy createRefundPolicy(Long theaterId, String name, Integer hoursBeforeShow,
                                            Integer refundPercentage) {
        Theater theater = theaterRepository.findById(theaterId)
                .orElseThrow(() -> new ResourceNotFoundException("Theater not found with id: " + theaterId));
        RefundPolicy policy = RefundPolicy.builder()
                .name(name)
                .theater(theater)
                .hoursBeforeShow(hoursBeforeShow)
                .refundPercentage(refundPercentage)
                .build();
        log.info("Creating refund policy: {} for theater id={}", name, theaterId);
        return refundPolicyRepository.save(policy);
    }

    public List<RefundPolicy> getRefundPolicies() {
        return refundPolicyRepository.findAll();
    }

    @Transactional
    public RefundPolicy updateRefundPolicy(Long id, String name, Integer hoursBeforeShow,
                                            Integer refundPercentage) {
        RefundPolicy policy = refundPolicyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RefundPolicy not found with id: " + id));
        if (name != null) policy.setName(name);
        if (hoursBeforeShow != null) policy.setHoursBeforeShow(hoursBeforeShow);
        if (refundPercentage != null) policy.setRefundPercentage(refundPercentage);
        log.info("Updating refund policy id={}", id);
        return refundPolicyRepository.save(policy);
    }

    @Transactional
    public void deleteRefundPolicy(Long id) {
        RefundPolicy policy = refundPolicyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RefundPolicy not found with id: " + id));
        log.info("Deleting refund policy id={}", id);
        refundPolicyRepository.delete(policy);
    }

    public BigDecimal calculateRefund(Booking booking) {
        Long theaterId = booking.getShow().getScreen().getTheater().getId();
        LocalDate showDate = booking.getShow().getDate();
        LocalTime showStartTime = booking.getShow().getStartTime();
        LocalDateTime showDateTime = LocalDateTime.of(showDate, showStartTime);

        long hoursUntilShow = ChronoUnit.HOURS.between(LocalDateTime.now(), showDateTime);

        List<RefundPolicy> policies = refundPolicyRepository.findByTheaterIdOrderByHoursBeforeShowDesc(theaterId);

        // Find the applicable policy (highest hoursBeforeShow that is <= actual hours remaining)
        RefundPolicy applicablePolicy = null;
        for (RefundPolicy policy : policies) {
            if (policy.getHoursBeforeShow() <= hoursUntilShow) {
                applicablePolicy = policy;
                break;
            }
        }

        if (applicablePolicy == null) {
            throw new RefundNotAllowedException(
                    "No refund policy applicable. Show starts in " + hoursUntilShow + " hours");
        }

        BigDecimal refundAmount = booking.getTotalAmount()
                .multiply(BigDecimal.valueOf(applicablePolicy.getRefundPercentage()))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        log.info("Calculated refund for booking id={}: amount={}, policy={}", 
                booking.getId(), refundAmount, applicablePolicy.getName());
        return refundAmount;
    }
}
