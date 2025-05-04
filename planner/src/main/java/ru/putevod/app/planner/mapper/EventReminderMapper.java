package ru.putevod.app.planner.mapper;

import org.mapstruct.*;
import ru.putevod.app.planner.dto.EventReminderDto;
import ru.putevod.app.planner.model.Event;
import ru.putevod.app.planner.model.EventReminder;
import ru.putevod.app.planner.model.User;

@Mapper(
    config = MapstructConfig.class,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface EventReminderMapper {
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(source = "reminderId", target = "id")
    @Mapping(source = "event.eventId", target = "eventId")
    @Mapping(source = "user.userId", target = "userId")
    EventReminderDto toDto(EventReminder eventReminder);
    
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(source = "id", target = "reminderId")
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    EventReminder toEntity(EventReminderDto eventReminderDto);
    
    default EventReminder fromDto(EventReminderDto eventReminderDto, Event event, User user) {
        if (eventReminderDto == null) {
            return null;
        }
        
        EventReminder eventReminder = toEntity(eventReminderDto);
        eventReminder.setEvent(event);
        eventReminder.setUser(user);
        
        return eventReminder;
    }
} 