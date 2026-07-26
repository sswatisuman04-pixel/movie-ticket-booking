package com.moviebooking.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreateRefundPolicyRequest {

    @NotBlank
    private String name;

    @NotNull
    @Positive
    private Integer hoursBeforeShow;

    @NotNull
    @Min(0)
    @Max(100)
    private Integer refundPercentage;

    @NotNull
    private Long theaterId;
}
