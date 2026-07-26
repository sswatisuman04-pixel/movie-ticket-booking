package com.moviebooking.dto.response;

import com.moviebooking.enums.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscountCodeResponse {

    private Long id;
    private String code;
    private DiscountType type;
    private BigDecimal value;
    private Integer maxUses;
    private Integer currentUses;
    private LocalDate validFrom;
    private LocalDate validTo;
    private BigDecimal minBookingAmount;
}
