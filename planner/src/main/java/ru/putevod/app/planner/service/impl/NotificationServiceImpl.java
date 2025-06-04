package ru.putevod.app.planner.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.putevod.app.planner.dto.NotificationDto;
import ru.putevod.app.planner.exception.BadRequestException;
import ru.putevod.app.planner.exception.ResourceNotFoundException;
import ru.putevod.app.planner.mapper.NotificationMapper;
import ru.putevod.app.planner.model.Notification;
import ru.putevod.app.planner.model.User;
import ru.putevod.app.planner.repository.NotificationRepository;
import ru.putevod.app.planner.service.NotificationService;
import ru.putevod.app.planner.service.UserService;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserService userService;
    private final NotificationMapper notificationMapper;

    @Override
    @Transactional
    public NotificationDto createNotification(Long userId, String type, String content, Integer relatedId) {
        User user = userService.getUserEntityById(userId);

        Notification notification = notificationMapper.createNotification(type, content, relatedId, user);
        notification = notificationRepository.save(notification);

        return notificationMapper.toDto(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationDto getNotification(Long userId, Long notificationId) {
        User user = userService.getUserEntityById(userId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Уведомление", "id", notificationId));

        if (!notification.getUser().getUserId().equals(userId)) {
            throw new BadRequestException("У вас нет доступа к этому уведомлению");
        }

        return notificationMapper.toDto(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDto> getUserNotifications(Long userId, Pageable pageable) {
        User user = userService.getUserEntityById(userId);

        Page<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable);

        return notifications.map(notificationMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public int getUnreadCount(Long userId) {
        User user = userService.getUserEntityById(userId);

        return notificationRepository.countUnreadByUser(user);
    }

    @Override
    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        User user = userService.getUserEntityById(userId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Уведомление", "id", notificationId));

        if (!notification.getUser().getUserId().equals(userId)) {
            throw new BadRequestException("У вас нет доступа к этому уведомлению");
        }

        notificationRepository.markAsRead(user, notificationId);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        User user = userService.getUserEntityById(userId);

        notificationRepository.markAllAsRead(user);
    }

    @Override
    @Transactional
    public void createTripInviteNotification(Long userId, Long tripId, String inviterUsername) {
        try {
            String content = "Пользователь " + inviterUsername + " пригласил вас присоединиться к поездке";
            Integer relatedId = tripId > Integer.MAX_VALUE ? null : tripId.intValue();
            createNotification(userId, "trip_invite", content, relatedId);
            log.info("Создано уведомление о приглашении в поездку {} для пользователя {} от {}",
                    tripId, userId, inviterUsername);
        } catch (Exception e) {
            log.error("Ошибка при создании уведомления о приглашении в поездку: {}", e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void createTripShareAcceptedNotification(Long tripOwnerId, Long tripId, String username) {
        try {
            String content = "Пользователь " + username + " принял ваше приглашение к поездке";
            Integer relatedId = tripId > Integer.MAX_VALUE ? null : tripId.intValue();
            createNotification(tripOwnerId, "trip_share_accepted", content, relatedId);
            log.info("Создано уведомление о принятии приглашения в поездку {} для владельца {} от {}",
                    tripId, tripOwnerId, username);
        } catch (Exception e) {
            log.error("Ошибка при создании уведомления о принятии приглашения: {}", e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void createTripInviteCancelledNotification(Long userId, Long tripId, String ownerUsername) {
        try {
            String content = "Пользователь " + ownerUsername + " отменил ваше приглашение к поездке";
            Integer relatedId = tripId > Integer.MAX_VALUE ? null : tripId.intValue();
            createNotification(userId, "trip_invite_cancelled", content, relatedId);
            log.info("Создано уведомление об отмене приглашения в поездку {} для пользователя {} от {}",
                    tripId, userId, ownerUsername);
        } catch (Exception e) {
            log.error("Ошибка при создании уведомления об отмене приглашения: {}", e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void createEventReminderNotification(Long userId, Long eventId, String eventTitle) {
        try {
            String content = "Напоминание о событии: " + eventTitle;
            Integer relatedId = eventId > Integer.MAX_VALUE ? null : eventId.intValue();
            createNotification(userId, "event_reminder", content, relatedId);
            log.info("Создано напоминание о событии {} для пользователя {}", eventId, userId);
        } catch (Exception e) {
            log.error("Ошибка при создании напоминания о событии: {}", e.getMessage(), e);
        }
    }
} 