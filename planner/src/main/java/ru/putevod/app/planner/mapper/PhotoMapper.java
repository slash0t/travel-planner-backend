package ru.putevod.app.planner.mapper;

import org.mapstruct.*;
import ru.putevod.app.planner.dto.PhotoDto;
import ru.putevod.app.planner.model.Photo;

@Mapper(componentModel = "spring", 
        uses = {FileMapper.class, UserMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PhotoMapper {
    
    @Mapping(source = "photoId", target = "id")
    @Mapping(source = "user.userId", target = "userId")
    @Mapping(source = "place.placeId", target = "placeId")
    @Mapping(source = "event.eventId", target = "eventId")
    @Mapping(source = "trip.tripId", target = "tripId")
    PhotoDto toDto(Photo photo);
    
    @Mapping(source = "id", target = "photoId")
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "place", ignore = true)
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "trip", ignore = true)
    @Mapping(target = "file", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Photo toEntity(PhotoDto photoDto);
} 