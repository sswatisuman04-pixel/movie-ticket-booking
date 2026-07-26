package com.moviebooking.dto.request;

import com.moviebooking.enums.SeatType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreateSeatRequest {

    @NotBlank
    private String row;

    @Positive
    private Integer number;

    @NotNull
    private SeatType seatType;
}
