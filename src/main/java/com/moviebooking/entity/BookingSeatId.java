package com.moviebooking.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class BookingSeatId implements Serializable {

    private Long bookingId;
    private Long showSeatId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookingSeatId that = (BookingSeatId) o;
        return Objects.equals(bookingId, that.bookingId) &&
                Objects.equals(showSeatId, that.showSeatId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookingId, showSeatId);
    }
}
