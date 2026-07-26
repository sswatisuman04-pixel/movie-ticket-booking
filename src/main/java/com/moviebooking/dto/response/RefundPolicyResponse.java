package com.moviebooking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundPolicyResponse {

    private Long id;
    private String name;
    private Integer hoursBeforeShow;
    private Integer refundPercentage;
    private Long theaterId;
}
