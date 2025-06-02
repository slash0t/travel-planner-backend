package ru.putevod.app.planner.mapper;

import org.mapstruct.*;
import ru.putevod.app.planner.dto.TripDayDto;
import ru.putevod.app.planner.dto.TripDto;
import ru.putevod.app.planner.model.Trip;

import java.time.temporal.ChronoUnit;
import java.util.Comparator;

@Mapper(componentModel = "spring",
        uses = {UserMapper.class, TripDayMapper.class, TripAccessMapper.class, FileMapper.class, TodoListMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TripMapper {

    @Mapping(source = "tripId", target = "id")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "totalDays", ignore = true)
    TripDto toDto(Trip trip);

    @AfterMapping
    default void computeDerivedFields(@MappingTarget TripDto tripDto, Trip trip) {
        tripDto.setStatus(trip.getStatus());

        if (tripDto.getStartDate() != null && tripDto.getEndDate() != null) {
            long daysBetween = ChronoUnit.DAYS.between(tripDto.getStartDate(), tripDto.getEndDate()) + 1;
            tripDto.setTotalDays((int) daysBetween);
        }

        if (tripDto.getDays() != null && !tripDto.getDays().isEmpty()) {
            tripDto.getDays().sort(Comparator.comparing(TripDayDto::getDayNumber));
        }
    }

    @Mapping(source = "id", target = "tripId")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "startDate", target = "startDate")
    @Mapping(source = "endDate", target = "endDate")
    @Mapping(source = "country", target = "country")
    @Mapping(source = "city", target = "city")
    @Mapping(source = "published", target = "published")
    @Mapping(source = "previewUrl", target = "previewUrl")
    @Mapping(target = "isDeleted", constant = "false")
    @Mapping(target = "days", ignore = true)
    @Mapping(target = "accesses", ignore = true)
    @Mapping(target = "files", ignore = true)
    @Mapping(target = "todoLists", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Trip toEntity(TripDto tripDto);

    @Mapping(target = "tripId", ignore = true)
    @Mapping(source = "title", target = "title")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "startDate", target = "startDate")
    @Mapping(source = "endDate", target = "endDate")
    @Mapping(source = "country", target = "country")
    @Mapping(source = "city", target = "city")
    @Mapping(source = "published", target = "published")
    @Mapping(source = "previewUrl", target = "previewUrl")
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "days", ignore = true)
    @Mapping(target = "accesses", ignore = true)
    @Mapping(target = "files", ignore = true)
    @Mapping(target = "todoLists", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void updateEntityFromDto(TripDto tripDto, @MappingTarget Trip trip);
} 