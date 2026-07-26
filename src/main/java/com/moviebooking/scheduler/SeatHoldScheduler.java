package com.moviebooking.scheduler;

import com.moviebooking.repository.ShowSeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class SeatHoldScheduler {

    private final ShowSeatRepository showSeatRepository;

    @Scheduled(fixedRateString = "${app.booking.hold-cleanup-interval-ms:30000}")
    @Transactional
    public void releaseExpiredHolds() {
        int released = showSeatRepository.releaseExpiredHolds(LocalDateTime.now());
        if (released > 0) {
            log.info("Released {} expired seat holds", released);
        }
    }
}
