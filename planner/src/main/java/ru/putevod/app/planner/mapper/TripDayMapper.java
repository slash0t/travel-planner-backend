package ru.putevod.app.planner.mapper;

import org.mapstruct.*;
import ru.putevod.app.planner.dto.TripDayDto;
import ru.putevod.app.planner.model.Trip;
import ru.putevod.app.planner.model.TripDay;

@Mapper(componentModel = "spring",
        uses = {EventMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        collectionMappingStrategy = CollectionMappingStrategy.ACCESSOR_ONLY)
public interface TripDayMapper {

    @Mapping(source = "dayId", target = "id")
    @Mapping(source = "trip.tripId", target = "tripId")
    TripDayDto toDto(TripDay tripDay);

    @Mapping(source = "id", target = "dayId")
    @Mapping(source = "dayNumber", target = "dayNumber")
    @Mapping(source = "date", target = "date")
    @Mapping(source = "note", target = "note")
    @Mapping(target = "trip", ignore = true)
    @Mapping(target = "events", expression = "java(null)")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    TripDay toEntity(TripDayDto tripDayDto);

    @Mapping(target = "dayId", ignore = true)
    @Mapping(source = "dayNumber", target = "dayNumber")
    @Mapping(source = "date", target = "date")
    @Mapping(source = "note", target = "note")
    @Mapping(target = "trip", ignore = true)
    @Mapping(target = "events", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(TripDayDto tripDayDto, @MappingTarget TripDay tripDay);

    default TripDay fromDto(TripDayDto tripDayDto, Trip trip) {
        if (tripDayDto == null) {
            return null;
        }

        TripDay tripDay = toEntity(tripDayDto);
        tripDay.setTrip(trip);

        return tripDay;
    }
}