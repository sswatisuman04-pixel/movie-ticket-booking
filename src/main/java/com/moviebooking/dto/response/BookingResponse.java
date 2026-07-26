package com.moviebooking.dto.response;

import com.moviebooking.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {

    private Long id;
    private Long showId;
    private String movieName;
    private String theaterName;
    private LocalDate date;
    private LocalTime startTime;
    private BookingStatus status;
    private BigDecimal totalAmount;
    private BigDecimal discountApplied;
    private List<SeatResponse> seats;
    private LocalDateTime bookingTime;
}
