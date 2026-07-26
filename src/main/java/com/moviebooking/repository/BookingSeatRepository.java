package com.moviebooking.repository;

import com.moviebooking.entity.BookingSeat;
import com.moviebooking.entity.BookingSeatId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingSeatRepository extends JpaRepository<BookingSeat, BookingSeatId> {

    List<BookingSeat> findByBookingId(Long bookingId);
}
