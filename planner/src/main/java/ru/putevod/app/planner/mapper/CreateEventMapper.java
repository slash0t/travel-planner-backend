package ru.putevod.app.planner.mapper;

import org.mapstruct.*;
import ru.putevod.app.planner.dto.CreateEventDto;
import ru.putevod.app.planner.dto.PlaceDto;
import ru.putevod.app.planner.model.Event;
import ru.putevod.app.planner.model.Place;
import ru.putevod.app.planner.model.TripDay;

@Mapper(
    config = MapstructConfig.class,
    uses = {PlaceMapper.class},
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface CreateEventMapper {
    
    /**
     * Преобразует PlaceInfo (из CreateEventDto) в PlaceDto
     */
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "name", target = "name")
    @Mapping(source = "latitude", target = "latitude")
    @Mapping(source = "longitude", target = "longitude")
    @Mapping(source = "address", target = "address")
    @Mapping(source = "placeType", target = "placeType")
    @Mapping(source = "externalId", target = "externalId")
    @Mapping(source = "previewUrl", target = "previewUrl")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "photos", ignore = true)
    PlaceDto toPlaceDto(CreateEventDto.PlaceInfo placeInfo);

    /**
     * Преобразует CreateEventDto в Event. 
     * Параметр day используется для связывания Event с TripDay.
     * Place создается только если в CreateEventDto есть информация о месте.
     */
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(target = "eventId", ignore = true)
    @Mapping(source = "title", target = "title")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "startTime", target = "startTime")
    @Mapping(source = "endTime", target = "endTime")
    @Mapping(source = "hasSpecificTime", target = "hasSpecificTime")
    @Mapping(source = "notes", target = "notes")
    @Mapping(source = "orderPosition", target = "orderPosition")
    @Mapping(target = "place", ignore = true)
    @Mapping(target = "files", ignore = true)
    @Mapping(target = "reminders", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Event toEventEntity(CreateEventDto createEventDto);
    
    /**
     * Создает Event из CreateEventDto и связывает его с TripDay
     */
    default Event fromDto(CreateEventDto createEventDto, TripDay day) {
        if (createEventDto == null) {
            return null;
        }
        
        Event event = toEventEntity(createEventDto);
        event.setDay(day);
        
        return event;
    }
} 