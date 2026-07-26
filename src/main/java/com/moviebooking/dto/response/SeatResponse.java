package com.moviebooking.dto.response;

import com.moviebooking.enums.SeatType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatResponse {

    private Long id;
    private String row;
    private Integer number;
    private SeatType seatType;
}
