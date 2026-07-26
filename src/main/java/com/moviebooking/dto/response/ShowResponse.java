package com.moviebooking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowResponse {

    private Long id;
    private String movieName;
    private Long screenId;
    private String screenName;
    private String theaterName;
    private String cityName;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDate date;
}
