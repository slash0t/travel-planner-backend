package ru.putevod.app.planner.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.putevod.app.planner.config.CurrentUser;
import ru.putevod.app.planner.dto.NotificationDto;
import ru.putevod.app.planner.service.NotificationService;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notifications", description = "API для управления уведомлениями")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {
    
    private final NotificationService notificationService;
    
    @GetMapping
    @Operation(summary = "Получить уведомления пользователя")
    public ResponseEntity<Page<NotificationDto>> getUserNotifications(
            @CurrentUser Long userId,
            Pageable pageable) {
        return ResponseEntity.ok(notificationService.getUserNotifications(userId, pageable));
    }
    
    @GetMapping("/unread-count")
    @Operation(summary = "Получить количество непрочитанных уведомлений")
    public ResponseEntity<Integer> getUnreadCount(
            @CurrentUser Long userId) {
        return ResponseEntity.ok(notificationService.getUnreadCount(userId));
    }
    
    @GetMapping("/{notificationId}")
    @Operation(summary = "Получить уведомление по ID")
    public ResponseEntity<NotificationDto> getNotification(
            @CurrentUser Long userId,
            @PathVariable Long notificationId) {
        return ResponseEntity.ok(notificationService.getNotification(userId, notificationId));
    }
    
    @PutMapping("/{notificationId}/read")
    @Operation(summary = "Отметить уведомление как прочитанное")
    public ResponseEntity<Void> markAsRead(
            @CurrentUser Long userId,
            @PathVariable Long notificationId) {
        notificationService.markAsRead(userId, notificationId);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/mark-all-read")
    @Operation(summary = "Отметить все уведомления как прочитанные")
    public ResponseEntity<Void> markAllAsRead(
            @CurrentUser Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.noContent().build();
    }
} 