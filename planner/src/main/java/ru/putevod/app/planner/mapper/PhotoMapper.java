package ru.putevod.app.planner.mapper;

import org.mapstruct.*;
import ru.putevod.app.planner.dto.PhotoDto;
import ru.putevod.app.planner.model.Photo;

@Mapper(
        config = MapstructConfig.class,
        uses = {FileMapper.class, UserMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PhotoMapper {
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(source = "photoId", target = "id")
    @Mapping(source = "user.userId", target = "userId")
    @Mapping(source = "place.placeId", target = "placeId")
    @Mapping(source = "event.eventId", target = "eventId")
    @Mapping(source = "trip.tripId", target = "tripId")
    @Mapping(target = "url", ignore = true)
    PhotoDto toDto(Photo photo);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(source = "id", target = "photoId")
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "place", ignore = true)
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "trip", ignore = true)
    @Mapping(target = "file", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Photo toEntity(PhotoDto photoDto);
} 