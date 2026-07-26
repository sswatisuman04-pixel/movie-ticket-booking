package com.moviebooking.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingCancelledEvent {

    private Long bookingId;
    private Long userId;
    private Long showId;
}
