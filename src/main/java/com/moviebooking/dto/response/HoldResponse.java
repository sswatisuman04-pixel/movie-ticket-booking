package com.moviebooking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoldResponse {

    private Long showId;
    private List<ShowSeatResponse> heldSeats;
    private LocalDateTime holdExpiresAt;
    private String message;
}
