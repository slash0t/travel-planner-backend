package ru.putevod.app.planner.mapper;

import org.mapstruct.*;
import ru.putevod.app.planner.dto.CreateEventDto;
import ru.putevod.app.planner.dto.EventDto;
import ru.putevod.app.planner.dto.UpdateEventDto;
import ru.putevod.app.planner.model.Event;
import ru.putevod.app.planner.model.TripDay;

@Mapper(
        config = MapstructConfig.class,
        uses = {PlaceMapper.class, FileMapper.class, EventReminderMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface EventMapper {

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(source = "eventId", target = "id")
    @Mapping(source = "day.dayId", target = "dayId")
    EventDto toDto(Event event);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(source = "id", target = "eventId")
    @Mapping(source = "place", target = "place")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "startTime", target = "startTime")
    @Mapping(source = "endTime", target = "endTime")
    @Mapping(source = "hasSpecificTime", target = "hasSpecificTime")
    @Mapping(source = "notes", target = "notes")
    @Mapping(source = "orderPosition", target = "orderPosition")
    @Mapping(target = "day", ignore = true)
    @Mapping(target = "files", ignore = true)
    @Mapping(target = "reminders", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Event toEntity(EventDto eventDto);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(target = "eventId", ignore = true)
    @Mapping(source = "place", target = "place")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "startTime", target = "startTime")
    @Mapping(source = "endTime", target = "endTime")
    @Mapping(source = "hasSpecificTime", target = "hasSpecificTime")
    @Mapping(source = "notes", target = "notes")
    @Mapping(source = "orderPosition", target = "orderPosition")
    @Mapping(target = "day", ignore = true)
    @Mapping(target = "files", ignore = true)
    @Mapping(target = "reminders", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(EventDto eventDto, @MappingTarget Event event);

    // Маппинг из CreateEventDto в Event
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(target = "eventId", ignore = true)
    @Mapping(source = "createEventDto.title", target = "title")
    @Mapping(source = "createEventDto.description", target = "description")
    @Mapping(source = "createEventDto.startTime", target = "startTime")
    @Mapping(source = "createEventDto.endTime", target = "endTime")
    @Mapping(source = "createEventDto.hasSpecificTime", target = "hasSpecificTime")
    @Mapping(source = "createEventDto.notes", target = "notes")
    @Mapping(source = "createEventDto.orderPosition", target = "orderPosition")
    @Mapping(source = "day", target = "day")
    @Mapping(target = "place", ignore = true)
    @Mapping(target = "files", ignore = true)
    @Mapping(target = "reminders", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Event toEntityFromCreate(CreateEventDto createEventDto, TripDay day);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(source = "title", target = "title")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "startTime", target = "startTime")
    @Mapping(source = "endTime", target = "endTime")
    @Mapping(source = "hasSpecificTime", target = "hasSpecificTime")
    @Mapping(source = "notes", target = "notes")
    @Mapping(source = "orderPosition", target = "orderPosition")
    @Mapping(target = "eventId", ignore = true)
    @Mapping(target = "day", ignore = true)
    @Mapping(target = "files", ignore = true)
    @Mapping(target = "reminders", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromUpdate(UpdateEventDto updateEventDto, @MappingTarget Event event);

    default Event fromDto(EventDto eventDto, TripDay day) {
        if (eventDto == null) {
            return null;
        }

        Event event = toEntity(eventDto);
        event.setDay(day);

        return event;
    }
} 