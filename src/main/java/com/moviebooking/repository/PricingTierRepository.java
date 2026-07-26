package com.moviebooking.repository;

import com.moviebooking.entity.PricingTier;
import com.moviebooking.enums.SeatType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PricingTierRepository extends JpaRepository<PricingTier, Long> {

    List<PricingTier> findByTheaterIdAndSeatType(Long theaterId, SeatType seatType);

    List<PricingTier> findByTheaterId(Long theaterId);
}
