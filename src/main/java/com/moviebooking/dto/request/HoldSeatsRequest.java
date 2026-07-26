package com.moviebooking.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class HoldSeatsRequest {

    @NotNull
    private Long showId;

    @NotEmpty
    private List<Long> showSeatIds;
}
