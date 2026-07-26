package com.moviebooking.dto.response;

import com.moviebooking.enums.SeatType;
import com.moviebooking.enums.ShowSeatStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowSeatResponse {

    private Long id;
    private Long seatId;
    private String row;
    private Integer number;
    private SeatType seatType;
    private ShowSeatStatus status;
    private BigDecimal price;
}
