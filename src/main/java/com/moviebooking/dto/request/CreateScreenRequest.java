package com.moviebooking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreateScreenRequest {

    @NotBlank
    private String name;

    @Positive
    private Integer totalSeats;
}
