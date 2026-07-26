package com.moviebooking.scheduler;

import com.moviebooking.entity.Booking;
import com.moviebooking.entity.Show;
import com.moviebooking.enums.BookingStatus;
import com.moviebooking.event.BookingReminderEvent;
import com.moviebooking.repository.BookingRepository;
import com.moviebooking.repository.ShowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReminderScheduler {

    private final BookingRepository bookingRepository;
    private final ShowRepository showRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Scheduled(fixedRate = 3600000) // Every hour
    public void sendReminders() {
        log.info("Checking for upcoming shows to send reminders...");

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        LocalTime twoHoursLater = now.plusHours(2);

        // Find all shows for today
        List<Show> allShows = showRepository.findAll();

        List<Show> upcomingShows = allShows.stream()
                .filter(show -> show.getDate().equals(today))
                .filter(show -> show.getStartTime().isAfter(now) && show.getStartTime().isBefore(twoHoursLater))
                .toList();

        if (upcomingShows.isEmpty()) {
            log.info("No upcoming shows within the next 2 hours");
            return;
        }

        log.info("Found {} shows starting within the next 2 hours", upcomingShows.size());

        // For each upcoming show, find confirmed bookings and publish reminder events
        List<Booking> allBookings = bookingRepository.findAll();

        for (Show show : upcomingShows) {
            List<Booking> confirmedBookings = allBookings.stream()
                    .filter(booking -> booking.getShow().getId().equals(show.getId()))
                    .filter(booking -> booking.getStatus() == BookingStatus.CONFIRMED)
                    .toList();

            for (Booking booking : confirmedBookings) {
                eventPublisher.publishEvent(
                        new BookingReminderEvent(booking.getId(), booking.getUser().getId(), show.getId()));
                log.info("Published reminder for booking id={}, show id={}", booking.getId(), show.getId());
            }
        }
    }
}
