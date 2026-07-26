package com.moviebooking.service;

import com.moviebooking.entity.DiscountCode;
import com.moviebooking.enums.DiscountType;
import com.moviebooking.exception.InvalidDiscountCodeException;
import com.moviebooking.exception.ResourceNotFoundException;
import com.moviebooking.repository.DiscountCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscountService {

    private final DiscountCodeRepository discountCodeRepository;

    @Transactional
    public DiscountCode createDiscountCode(String code, DiscountType type, BigDecimal value,
                                            Integer maxUses, LocalDate validFrom, LocalDate validTo,
                                            BigDecimal minBookingAmount) {
        DiscountCode discountCode = DiscountCode.builder()
                .code(code)
                .type(type)
                .value(value)
                .maxUses(maxUses)
                .currentUses(0)
                .validFrom(validFrom)
                .validTo(validTo)
                .minBookingAmount(minBookingAmount)
                .build();
        log.info("Creating discount code: {}", code);
        return discountCodeRepository.save(discountCode);
    }

    public List<DiscountCode> getDiscountCodes() {
        return discountCodeRepository.findAll();
    }

    @Transactional
    public DiscountCode updateDiscountCode(Long id, String code, DiscountType type, BigDecimal value,
                                            Integer maxUses, LocalDate validFrom, LocalDate validTo,
                                            BigDecimal minBookingAmount) {
        DiscountCode discountCode = discountCodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Discount code not found with id: " + id));
        if (code != null) discountCode.setCode(code);
        if (type != null) discountCode.setType(type);
        if (value != null) discountCode.setValue(value);
        if (maxUses != null) discountCode.setMaxUses(maxUses);
        if (validFrom != null) discountCode.setValidFrom(validFrom);
        if (validTo != null) discountCode.setValidTo(validTo);
        if (minBookingAmount != null) discountCode.setMinBookingAmount(minBookingAmount);
        log.info("Updating discount code id={}", id);
        return discountCodeRepository.save(discountCode);
    }

    @Transactional
    public void deleteDiscountCode(Long id) {
        DiscountCode discountCode = discountCodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Discount code not found with id: " + id));
        log.info("Deleting discount code id={}", id);
        discountCodeRepository.delete(discountCode);
    }

    @Transactional
    public BigDecimal validateAndApplyDiscount(String code, BigDecimal totalAmount) {
        DiscountCode discountCode = discountCodeRepository.findByCode(code)
                .orElseThrow(() -> new InvalidDiscountCodeException("Discount code '" + code + "' not found"));

        LocalDate today = LocalDate.now();
        if (today.isBefore(discountCode.getValidFrom()) || today.isAfter(discountCode.getValidTo())) {
            throw new InvalidDiscountCodeException("Discount code '" + code + "' is not valid at this time");
        }

        if (discountCode.getCurrentUses() >= discountCode.getMaxUses()) {
            throw new InvalidDiscountCodeException("Discount code '" + code + "' has exceeded maximum uses");
        }

        if (discountCode.getMinBookingAmount() != null
                && totalAmount.compareTo(discountCode.getMinBookingAmount()) < 0) {
            throw new InvalidDiscountCodeException(
                    "Minimum booking amount of " + discountCode.getMinBookingAmount() + " not met");
        }

        BigDecimal discountAmount;
        if (discountCode.getType() == DiscountType.PERCENTAGE) {
            discountAmount = totalAmount.multiply(discountCode.getValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            discountAmount = discountCode.getValue().min(totalAmount);
        }

        // Increment uses atomically
        discountCode.setCurrentUses(discountCode.getCurrentUses() + 1);
        discountCodeRepository.save(discountCode);

        log.info("Applied discount code '{}' for amount: {}", code, discountAmount);
        return discountAmount;
    }
}
