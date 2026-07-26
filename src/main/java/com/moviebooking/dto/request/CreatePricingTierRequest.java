package com.moviebooking.dto.request;

import com.moviebooking.enums.SeatType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePricingTierRequest {

    @NotBlank
    private String name;

    @NotNull
    private SeatType seatType;

    @NotNull
    @Positive
    private BigDecimal multiplier;

    @NotNull
    @Positive
    private BigDecimal basePrice;

    @NotBlank
    private String applicableDays;

    @NotNull
    private Long theaterId;
}
