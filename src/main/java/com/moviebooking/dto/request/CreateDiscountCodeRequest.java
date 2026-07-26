package com.moviebooking.dto.request;

import com.moviebooking.enums.DiscountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateDiscountCodeRequest {

    @NotBlank
    private String code;

    @NotNull
    private DiscountType type;

    @NotNull
    @Positive
    private BigDecimal value;

    @Positive
    private Integer maxUses;

    @NotNull
    private LocalDate validFrom;

    @NotNull
    private LocalDate validTo;

    private BigDecimal minBookingAmount;
}
