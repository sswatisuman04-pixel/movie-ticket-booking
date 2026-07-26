package com.moviebooking.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BulkCreateSeatsRequest {

    @NotEmpty
    @Valid
    private List<CreateSeatRequest> seats;
}
