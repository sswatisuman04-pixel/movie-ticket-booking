package com.moviebooking.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateTheaterRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String address;
}
