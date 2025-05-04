package ru.putevod.app.planner.mapper;

import org.mapstruct.*;
import ru.putevod.app.planner.dto.NotificationDto;
import ru.putevod.app.planner.model.Notification;
import ru.putevod.app.planner.model.User;

@Mapper(componentModel = "spring", 
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface NotificationMapper {
    
    @Mapping(source = "notificationId", target = "id")
    @Mapping(source = "user.userId", target = "userId")
    NotificationDto toDto(Notification notification);
    
    @Mapping(source = "id", target = "notificationId")
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Notification toEntity(NotificationDto notificationDto);
    
    default Notification createNotification(String type, String content, Integer relatedId, User user) {
        Notification notification = new Notification();
        notification.setType(type);
        notification.setContent(content);
        notification.setRelatedId(relatedId);
        notification.setUser(user);
        notification.setRead(false);
        
        return notification;
    }
} 