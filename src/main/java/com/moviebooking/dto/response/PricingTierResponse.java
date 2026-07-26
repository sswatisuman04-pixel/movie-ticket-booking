package com.moviebooking.dto.response;

import com.moviebooking.enums.SeatType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricingTierResponse {

    private Long id;
    private String name;
    private SeatType seatType;
    private BigDecimal multiplier;
    private BigDecimal basePrice;
    private String applicableDays;
    private Long theaterId;
}
