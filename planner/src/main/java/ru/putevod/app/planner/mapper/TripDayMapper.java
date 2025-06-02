package ru.putevod.app.planner.mapper;

import org.mapstruct.*;
import ru.putevod.app.planner.dto.CreateTripDayDto;
import ru.putevod.app.planner.dto.TripDayDto;
import ru.putevod.app.planner.model.Trip;
import ru.putevod.app.planner.model.TripDay;

@Mapper(
        config = MapstructConfig.class,
        uses = {EventMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        collectionMappingStrategy = CollectionMappingStrategy.ACCESSOR_ONLY
)
public interface TripDayMapper {

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(source = "dayId", target = "id")
    @Mapping(source = "trip.tripId", target = "tripId")
    TripDayDto toDto(TripDay tripDay);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(source = "id", target = "dayId")
    @Mapping(source = "dayNumber", target = "dayNumber")
    @Mapping(source = "date", target = "date")
    @Mapping(source = "note", target = "note")
    @Mapping(target = "trip", ignore = true)
    @Mapping(target = "events", expression = "java(null)")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    TripDay toEntity(TripDayDto tripDayDto);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(target = "dayId", ignore = true)
    @Mapping(source = "dayNumber", target = "dayNumber")
    @Mapping(source = "date", target = "date")
    @Mapping(source = "note", target = "note")
    @Mapping(target = "trip", ignore = true)
    @Mapping(target = "events", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(TripDayDto tripDayDto, @MappingTarget TripDay tripDay);

    // Маппинг из CreateTripDayDto в TripDay
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(target = "dayId", ignore = true)
    @Mapping(target = "dayNumber", ignore = true) // Будет вычислено в сервисе
    @Mapping(source = "createTripDayDto.date", target = "date")
    @Mapping(source = "createTripDayDto.note", target = "note")
    @Mapping(source = "trip", target = "trip")
    @Mapping(target = "events", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    TripDay toEntityFromCreate(CreateTripDayDto createTripDayDto, Trip trip);

    default TripDay fromDto(TripDayDto tripDayDto, Trip trip) {
        if (tripDayDto == null) {
            return null;
        }

        TripDay tripDay = toEntity(tripDayDto);
        tripDay.setTrip(trip);

        return tripDay;
    }

    default TripDay fromCreateDto(CreateTripDayDto createTripDayDto, Trip trip) {
        if (createTripDayDto == null) {
            return null;
        }

        return toEntityFromCreate(createTripDayDto, trip);
    }
}