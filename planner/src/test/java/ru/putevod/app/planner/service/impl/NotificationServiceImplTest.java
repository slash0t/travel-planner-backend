package ru.putevod.app.planner.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.putevod.app.planner.dto.NotificationDto;
import ru.putevod.app.planner.exception.BadRequestException;
import ru.putevod.app.planner.exception.ResourceNotFoundException;
import ru.putevod.app.planner.mapper.NotificationMapper;
import ru.putevod.app.planner.model.Notification;
import ru.putevod.app.planner.model.User;
import ru.putevod.app.planner.repository.NotificationRepository;
import ru.putevod.app.planner.service.UserService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserService userService;

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private User testUser;
    private Notification testNotification;
    private NotificationDto testNotificationDto;
    private Long userId;
    private Long notificationId;

    @BeforeEach
    void setUp() {
        userId = 1L;
        notificationId = 1L;

        testUser = new User();
        testUser.setUserId(userId);
        testUser.setUsername("testuser");

        testNotification = new Notification();
        testNotification.setNotificationId(notificationId);
        testNotification.setUser(testUser);
        testNotification.setType("test_type");
        testNotification.setContent("Test notification content");
        testNotification.setRelatedId(123);
        testNotification.setRead(false);
        testNotification.setCreatedAt(LocalDateTime.now());

        testNotificationDto = NotificationDto.builder()
                .id(notificationId)
                .userId(userId)
                .type("test_type")
                .content("Test notification content")
                .relatedId(123)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create notification successfully")
    void createNotification_Success() {
        when(userService.getUserEntityById(userId)).thenReturn(testUser);
        when(notificationMapper.createNotification(anyString(), anyString(), any(), any()))
                .thenReturn(testNotification);
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
        when(notificationMapper.toDto(any(Notification.class))).thenReturn(testNotificationDto);

        NotificationDto result = notificationService.createNotification(userId, "test_type", "Test content", 123);

        assertNotNull(result);
        assertEquals(testNotificationDto.getId(), result.getId());
        assertEquals(testNotificationDto.getType(), result.getType());
        assertEquals(testNotificationDto.getContent(), result.getContent());
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("Should get notification successfully")
    void getNotification_Success() {
        when(userService.getUserEntityById(userId)).thenReturn(testUser);
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(testNotification));
        when(notificationMapper.toDto(testNotification)).thenReturn(testNotificationDto);

        NotificationDto result = notificationService.getNotification(userId, notificationId);

        assertNotNull(result);
        assertEquals(testNotificationDto.getId(), result.getId());
        assertEquals(testNotificationDto.getType(), result.getType());
        assertEquals(testNotificationDto.getContent(), result.getContent());
    }

    @Test
    @DisplayName("Should throw exception when notification not found")
    void getNotification_NotFound_ThrowsException() {
        when(userService.getUserEntityById(userId)).thenReturn(testUser);
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                notificationService.getNotification(userId, notificationId));
    }

    @Test
    @DisplayName("Should throw exception when accessing another user's notification")
    void getNotification_WrongUser_ThrowsException() {
        User otherUser = new User();
        otherUser.setUserId(2L);
        testNotification.setUser(otherUser);

        when(userService.getUserEntityById(userId)).thenReturn(testUser);
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(testNotification));

        assertThrows(BadRequestException.class, () ->
                notificationService.getNotification(userId, notificationId));
    }

    @Test
    @DisplayName("Should get user notifications successfully")
    void getUserNotifications_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Notification> notifications = Arrays.asList(testNotification);
        Page<Notification> notificationPage = new PageImpl<>(notifications);

        when(userService.getUserEntityById(userId)).thenReturn(testUser);
        when(notificationRepository.findByUserOrderByCreatedAtDesc(testUser, pageable))
                .thenReturn(notificationPage);
        when(notificationMapper.toDto(testNotification)).thenReturn(testNotificationDto);

        Page<NotificationDto> result = notificationService.getUserNotifications(userId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testNotificationDto.getId(), result.getContent().get(0).getId());
    }

    @Test
    @DisplayName("Should get unread count successfully")
    void getUnreadCount_Success() {
        when(userService.getUserEntityById(userId)).thenReturn(testUser);
        when(notificationRepository.countUnreadByUser(testUser)).thenReturn(5);

        int result = notificationService.getUnreadCount(userId);

        assertEquals(5, result);
    }

    @Test
    @DisplayName("Should mark notification as read successfully")
    void markAsRead_Success() {
        when(userService.getUserEntityById(userId)).thenReturn(testUser);
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(testNotification));

        notificationService.markAsRead(userId, notificationId);

        verify(notificationRepository).markAsRead(testUser, notificationId);
    }

    @Test
    @DisplayName("Should mark all notifications as read successfully")
    void markAllAsRead_Success() {
        when(userService.getUserEntityById(userId)).thenReturn(testUser);

        notificationService.markAllAsRead(userId);

        verify(notificationRepository).markAllAsRead(testUser);
    }

    @Test
    @DisplayName("Should create trip invite notification successfully")
    void createTripInviteNotification_Success() {
        Long tripId = 1L;
        String inviterUsername = "inviter";

        when(userService.getUserEntityById(userId)).thenReturn(testUser);
        when(notificationMapper.createNotification(anyString(), anyString(), any(), any()))
                .thenReturn(testNotification);
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
        when(notificationMapper.toDto(any(Notification.class))).thenReturn(testNotificationDto);

        notificationService.createTripInviteNotification(userId, tripId, inviterUsername);

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("Should create trip share accepted notification successfully")
    void createTripShareAcceptedNotification_Success() {
        Long tripId = 1L;
        String username = "accepter";

        when(userService.getUserEntityById(userId)).thenReturn(testUser);
        when(notificationMapper.createNotification(anyString(), anyString(), any(), any()))
                .thenReturn(testNotification);
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
        when(notificationMapper.toDto(any(Notification.class))).thenReturn(testNotificationDto);

        notificationService.createTripShareAcceptedNotification(userId, tripId, username);

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("Should create trip invite cancelled notification successfully")
    void createTripInviteCancelledNotification_Success() {
        Long tripId = 1L;
        String ownerUsername = "owner";

        when(userService.getUserEntityById(userId)).thenReturn(testUser);
        when(notificationMapper.createNotification(anyString(), anyString(), any(), any()))
                .thenReturn(testNotification);
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
        when(notificationMapper.toDto(any(Notification.class))).thenReturn(testNotificationDto);

        notificationService.createTripInviteCancelledNotification(userId, tripId, ownerUsername);

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("Should create event reminder notification successfully")
    void createEventReminderNotification_Success() {
        Long eventId = 1L;
        String eventTitle = "Test Event";

        when(userService.getUserEntityById(userId)).thenReturn(testUser);
        when(notificationMapper.createNotification(anyString(), anyString(), any(), any()))
                .thenReturn(testNotification);
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
        when(notificationMapper.toDto(any(Notification.class))).thenReturn(testNotificationDto);

        notificationService.createEventReminderNotification(userId, eventId, eventTitle);

        verify(notificationRepository).save(any(Notification.class));
    }
} 