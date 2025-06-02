package ru.putevod.app.planner.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.putevod.app.planner.dto.NotificationDto;
import ru.putevod.app.planner.model.Notification;
import ru.putevod.app.planner.model.User;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class NotificationMapperTest {

    @Autowired
    private NotificationMapper notificationMapper;

    @Test
    void toDto_ShouldMapCorrectly() {
        User user = new User();
        user.setUserId(1L);
        user.setUsername("testuser");

        Notification notification = new Notification();
        notification.setNotificationId(1L);
        notification.setUser(user);
        notification.setType("test_type");
        notification.setContent("Test notification content");
        notification.setRelatedId(123);
        notification.setRead(false);

        NotificationDto result = notificationMapper.toDto(notification);

        assertNotNull(result);
        assertEquals(notification.getNotificationId(), result.getId());
        assertEquals(notification.getUser().getUserId(), result.getUserId());
        assertEquals(notification.getType(), result.getType());
        assertEquals(notification.getContent(), result.getContent());
        assertEquals(notification.getRelatedId(), result.getRelatedId());
        assertEquals(notification.isRead(), result.isRead());
    }

    @Test
    void toEntity_ShouldMapCorrectly() {
        NotificationDto notificationDto = new NotificationDto();
        notificationDto.setId(1L);
        notificationDto.setUserId(1L);
        notificationDto.setType("test_type");
        notificationDto.setContent("Test notification content");
        notificationDto.setRelatedId(123);
        notificationDto.setRead(false);

        Notification result = notificationMapper.toEntity(notificationDto);

        assertNotNull(result);
        assertEquals(notificationDto.getId(), result.getNotificationId());
        assertEquals(notificationDto.getType(), result.getType());
        assertEquals(notificationDto.getContent(), result.getContent());
        assertEquals(notificationDto.getRelatedId(), result.getRelatedId());
        assertEquals(notificationDto.isRead(), result.isRead());
    }

    @Test
    void createNotification_ShouldCreateCorrectly() {
        User user = new User();
        user.setUserId(1L);
        user.setUsername("testuser");
        String type = "test_type";
        String content = "Test notification content";
        Integer relatedId = 123;

        Notification result = notificationMapper.createNotification(type, content, relatedId, user);

        assertNotNull(result);
        assertEquals(type, result.getType());
        assertEquals(content, result.getContent());
        assertEquals(relatedId, result.getRelatedId());
        assertEquals(user, result.getUser());
        assertFalse(result.isRead());
    }
} 