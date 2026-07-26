package com.moviebooking.listener;

import com.moviebooking.enums.NotificationType;
import com.moviebooking.event.BookingCancelledEvent;
import com.moviebooking.event.BookingConfirmedEvent;
import com.moviebooking.event.BookingReminderEvent;
import com.moviebooking.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationListener {

    private final NotificationService notificationService;

    @Async
    @EventListener
    public void onBookingConfirmed(BookingConfirmedEvent event) {
        log.info("Received BookingConfirmedEvent for booking {}", event.getBookingId());
        notificationService.sendNotification(
                event.getUserId(), event.getBookingId(), NotificationType.BOOKING_CONFIRMED);
    }

    @Async
    @EventListener
    public void onBookingCancelled(BookingCancelledEvent event) {
        log.info("Received BookingCancelledEvent for booking {}", event.getBookingId());
        notificationService.sendNotification(
                event.getUserId(), event.getBookingId(), NotificationType.BOOKING_CANCELLED);
    }

    @Async
    @EventListener
    public void onBookingReminder(BookingReminderEvent event) {
        log.info("Received BookingReminderEvent for booking {}", event.getBookingId());
        notificationService.sendNotification(
                event.getUserId(), event.getBookingId(), NotificationType.REMINDER);
    }
}
