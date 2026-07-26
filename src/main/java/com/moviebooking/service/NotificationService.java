package com.moviebooking.service;

import com.moviebooking.entity.Booking;
import com.moviebooking.entity.Notification;
import com.moviebooking.entity.User;
import com.moviebooking.enums.NotificationChannel;
import com.moviebooking.enums.NotificationStatus;
import com.moviebooking.enums.NotificationType;
import com.moviebooking.exception.ResourceNotFoundException;
import com.moviebooking.repository.BookingRepository;
import com.moviebooking.repository.NotificationRepository;
import com.moviebooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    @Transactional
    public void sendNotification(Long userId, Long bookingId, NotificationType type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        Notification notification = Notification.builder()
                .user(user)
                .booking(booking)
                .type(type)
                .channel(NotificationChannel.EMAIL)
                .status(NotificationStatus.PENDING)
                .build();

        notification = notificationRepository.save(notification);

        // Simulate sending (log it)
        log.info("[NOTIFICATION] Sending {} to user {} (email: {}) for booking {}",
                type, user.getName(), user.getEmail(), bookingId);

        // Mark as sent
        notification.setStatus(NotificationStatus.SENT);
        notification.setSentAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    public List<Notification> getNotificationsByUser(Long userId) {
        return notificationRepository.findByUserId(userId);
    }
}
