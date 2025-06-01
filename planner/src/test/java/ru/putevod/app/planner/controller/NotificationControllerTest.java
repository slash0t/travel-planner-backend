package ru.putevod.app.planner.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.putevod.app.planner.dto.NotificationDto;
import ru.putevod.app.planner.service.NotificationService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    private Long userId;
    private Long notificationId;
    private NotificationDto mockNotificationDto;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        userId = 1L;
        notificationId = 1L;
        pageable = PageRequest.of(0, 10);

        mockNotificationDto = NotificationDto.builder()
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
    void getUserNotifications_ShouldReturnNotifications() {
        List<NotificationDto> notifications = Arrays.asList(mockNotificationDto);
        Page<NotificationDto> notificationPage = new PageImpl<>(notifications);
        when(notificationService.getUserNotifications(eq(userId), any(Pageable.class)))
                .thenReturn(notificationPage);

        ResponseEntity<Page<NotificationDto>> response = notificationController.getUserNotifications(userId, pageable);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        assertEquals(mockNotificationDto.getId(), response.getBody().getContent().get(0).getId());
        verify(notificationService).getUserNotifications(eq(userId), any(Pageable.class));
    }

    @Test
    void getUnreadCount_ShouldReturnCount() {
        int unreadCount = 5;
        when(notificationService.getUnreadCount(userId))
                .thenReturn(unreadCount);

        ResponseEntity<Integer> response = notificationController.getUnreadCount(userId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(unreadCount, response.getBody());
        verify(notificationService).getUnreadCount(userId);
    }

    @Test
    void getNotification_ShouldReturnNotification() {
        when(notificationService.getNotification(userId, notificationId))
                .thenReturn(mockNotificationDto);

        ResponseEntity<NotificationDto> response = notificationController.getNotification(userId, notificationId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockNotificationDto.getId(), response.getBody().getId());
        assertEquals(mockNotificationDto.getContent(), response.getBody().getContent());
        verify(notificationService).getNotification(userId, notificationId);
    }

    @Test
    void markAsRead_ShouldReturnNoContent() {
        doNothing().when(notificationService).markAsRead(userId, notificationId);

        ResponseEntity<Void> response = notificationController.markAsRead(userId, notificationId);

        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(notificationService).markAsRead(userId, notificationId);
    }

    @Test
    void markAllAsRead_ShouldReturnNoContent() {
        doNothing().when(notificationService).markAllAsRead(userId);

        ResponseEntity<Void> response = notificationController.markAllAsRead(userId);

        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(notificationService).markAllAsRead(userId);
    }
} 