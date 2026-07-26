package com.moviebooking.service;

import com.moviebooking.entity.PricingTier;
import com.moviebooking.entity.Seat;
import com.moviebooking.entity.Show;
import com.moviebooking.entity.Theater;
import com.moviebooking.enums.SeatType;
import com.moviebooking.exception.ResourceNotFoundException;
import com.moviebooking.repository.PricingTierRepository;
import com.moviebooking.repository.TheaterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PricingService {

    private static final BigDecimal DEFAULT_BASE_PRICE = new BigDecimal("200.00");

    private final PricingTierRepository pricingTierRepository;
    private final TheaterRepository theaterRepository;

    @Transactional
    public PricingTier createPricingTier(Long theaterId, String name, SeatType seatType,
                                          BigDecimal basePrice, BigDecimal multiplier,
                                          String applicableDays) {
        Theater theater = theaterRepository.findById(theaterId)
                .orElseThrow(() -> new ResourceNotFoundException("Theater not found with id: " + theaterId));
        PricingTier tier = PricingTier.builder()
                .name(name)
                .theater(theater)
                .seatType(seatType)
                .basePrice(basePrice)
                .multiplier(multiplier)
                .applicableDays(applicableDays)
                .build();
        log.info("Creating pricing tier: {} for theater id={}", name, theaterId);
        return pricingTierRepository.save(tier);
    }

    public List<PricingTier> getPricingTiers() {
        return pricingTierRepository.findAll();
    }

    @Transactional
    public PricingTier updatePricingTier(Long id, String name, SeatType seatType,
                                          BigDecimal basePrice, BigDecimal multiplier,
                                          String applicableDays) {
        PricingTier tier = pricingTierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PricingTier not found with id: " + id));
        if (name != null) tier.setName(name);
        if (seatType != null) tier.setSeatType(seatType);
        if (basePrice != null) tier.setBasePrice(basePrice);
        if (multiplier != null) tier.setMultiplier(multiplier);
        if (applicableDays != null) tier.setApplicableDays(applicableDays);
        log.info("Updating pricing tier id={}", id);
        return pricingTierRepository.save(tier);
    }

    @Transactional
    public void deletePricingTier(Long id) {
        PricingTier tier = pricingTierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PricingTier not found with id: " + id));
        log.info("Deleting pricing tier id={}", id);
        pricingTierRepository.delete(tier);
    }

    public BigDecimal calculatePrice(Seat seat, Show show) {
        Long theaterId = show.getScreen().getTheater().getId();
        SeatType seatType = seat.getSeatType();
        String dayOfWeek = show.getDate().getDayOfWeek().name();

        List<PricingTier> tiers = pricingTierRepository.findByTheaterIdAndSeatType(theaterId, seatType);

        for (PricingTier tier : tiers) {
            String days = tier.getApplicableDays();
            if (days != null && (days.equalsIgnoreCase("ALL")
                    || days.toUpperCase().contains(dayOfWeek))) {
                return tier.getBasePrice().multiply(tier.getMultiplier());
            }
        }

        // If no day-specific match, use first tier without day restriction
        for (PricingTier tier : tiers) {
            if (tier.getApplicableDays() == null || tier.getApplicableDays().isEmpty()) {
                return tier.getBasePrice().multiply(tier.getMultiplier());
            }
        }

        // Default base price if no tier matches
        log.debug("No pricing tier found for theater={}, seatType={}, day={}. Using default.", theaterId, seatType, dayOfWeek);
        return DEFAULT_BASE_PRICE;
    }
}
