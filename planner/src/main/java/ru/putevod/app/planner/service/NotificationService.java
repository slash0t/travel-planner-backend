package ru.putevod.app.planner.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.putevod.app.planner.dto.NotificationDto;

public interface NotificationService {

    NotificationDto createNotification(Long userId, String type, String content, Integer relatedId);

    NotificationDto getNotification(Long userId, Long notificationId);

    Page<NotificationDto> getUserNotifications(Long userId, Pageable pageable);

    int getUnreadCount(Long userId);

    void markAsRead(Long userId, Long notificationId);

    void markAllAsRead(Long userId);

    void createTripInviteNotification(Long userId, Long tripId, String inviterUsername);

    void createTripShareAcceptedNotification(Long tripOwnerId, Long tripId, String username);

    void createTripInviteCancelledNotification(Long userId, Long tripId, String ownerUsername);

    void createEventReminderNotification(Long userId, Long eventId, String eventTitle);
} 