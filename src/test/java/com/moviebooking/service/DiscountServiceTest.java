package com.moviebooking.service;

import com.moviebooking.entity.DiscountCode;
import com.moviebooking.enums.DiscountType;
import com.moviebooking.exception.InvalidDiscountCodeException;
import com.moviebooking.repository.DiscountCodeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiscountServiceTest {

    @Mock
    private DiscountCodeRepository discountCodeRepository;

    @InjectMocks
    private DiscountService discountService;

    @Test
    void validateAndApplyDiscount_percentage_shouldCalculateCorrectly() {
        DiscountCode code = DiscountCode.builder()
                .id(1L).code("SAVE20").type(DiscountType.PERCENTAGE)
                .value(new BigDecimal("20"))
                .maxUses(100).currentUses(5)
                .validFrom(LocalDate.now().minusDays(1))
                .validTo(LocalDate.now().plusDays(10))
                .minBookingAmount(null)
                .build();

        when(discountCodeRepository.findByCode("SAVE20")).thenReturn(Optional.of(code));
        when(discountCodeRepository.save(any())).thenReturn(code);

        BigDecimal discount = discountService.validateAndApplyDiscount("SAVE20", new BigDecimal("500.00"));

        // 500 * 20 / 100 = 100.00
        assertThat(discount).isEqualByComparingTo("100.00");
    }

    @Test
    void validateAndApplyDiscount_flat_shouldCapAtTotal() {
        DiscountCode code = DiscountCode.builder()
                .id(1L).code("FLAT500").type(DiscountType.FLAT)
                .value(new BigDecimal("500"))
                .maxUses(100).currentUses(0)
                .validFrom(LocalDate.now().minusDays(1))
                .validTo(LocalDate.now().plusDays(10))
                .minBookingAmount(null)
                .build();

        when(discountCodeRepository.findByCode("FLAT500")).thenReturn(Optional.of(code));
        when(discountCodeRepository.save(any())).thenReturn(code);

        // Total is 300, flat discount is 500, so capped at 300
        BigDecimal discount = discountService.validateAndApplyDiscount("FLAT500", new BigDecimal("300.00"));

        assertThat(discount).isEqualByComparingTo("300.00");
    }

    @Test
    void validateAndApplyDiscount_expired_shouldThrowInvalidCodeException() {
        DiscountCode code = DiscountCode.builder()
                .id(1L).code("EXPIRED").type(DiscountType.PERCENTAGE)
                .value(new BigDecimal("10"))
                .maxUses(100).currentUses(0)
                .validFrom(LocalDate.now().minusDays(30))
                .validTo(LocalDate.now().minusDays(1))
                .minBookingAmount(null)
                .build();

        when(discountCodeRepository.findByCode("EXPIRED")).thenReturn(Optional.of(code));

        assertThatThrownBy(() -> discountService.validateAndApplyDiscount("EXPIRED", new BigDecimal("500.00")))
                .isInstanceOf(InvalidDiscountCodeException.class)
                .hasMessageContaining("not valid at this time");
    }

    @Test
    void validateAndApplyDiscount_maxUsesExceeded_shouldThrowException() {
        DiscountCode code = DiscountCode.builder()
                .id(1L).code("MAXED").type(DiscountType.PERCENTAGE)
                .value(new BigDecimal("10"))
                .maxUses(5).currentUses(5)
                .validFrom(LocalDate.now().minusDays(1))
                .validTo(LocalDate.now().plusDays(10))
                .minBookingAmount(null)
                .build();

        when(discountCodeRepository.findByCode("MAXED")).thenReturn(Optional.of(code));

        assertThatThrownBy(() -> discountService.validateAndApplyDiscount("MAXED", new BigDecimal("500.00")))
                .isInstanceOf(InvalidDiscountCodeException.class)
                .hasMessageContaining("exceeded maximum uses");
    }
}
