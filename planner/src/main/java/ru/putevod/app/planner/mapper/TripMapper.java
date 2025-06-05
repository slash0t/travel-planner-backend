package ru.putevod.app.planner.mapper;

import org.mapstruct.*;
import ru.putevod.app.planner.dto.CreateTripDto;
import ru.putevod.app.planner.dto.TripDayDto;
import ru.putevod.app.planner.dto.TripDto;
import ru.putevod.app.planner.dto.UpdateTripDto;
import ru.putevod.app.planner.model.Trip;
import ru.putevod.app.planner.model.User;

import java.time.temporal.ChronoUnit;
import java.util.Comparator;

@Mapper(
        config = MapstructConfig.class,
        uses = {UserMapper.class, TripDayMapper.class, TripAccessMapper.class, FileMapper.class, TodoListMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface TripMapper {

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(source = "tripId", target = "id")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "totalDays", ignore = true)
    @Mapping(source = "todoLists", target = "todoLists", qualifiedByName = "toDtoWithTripCheck")
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

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(source = "id", target = "tripId")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "startDate", target = "startDate")
    @Mapping(source = "endDate", target = "endDate")
    @Mapping(source = "country", target = "country")
    @Mapping(source = "city", target = "city")
    @Mapping(source = "published", target = "published")
    @Mapping(source = "previewUrl", target = "previewUrl")
    @Mapping(target = "deleted", constant = "false")
    @Mapping(target = "days", ignore = true)
    @Mapping(target = "accesses", ignore = true)
    @Mapping(target = "files", ignore = true)
    @Mapping(target = "todoLists", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Trip toEntity(TripDto tripDto);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
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

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(target = "tripId", ignore = true)
    @Mapping(source = "createTripDto.title", target = "title")
    @Mapping(source = "createTripDto.description", target = "description")
    @Mapping(source = "createTripDto.startDate", target = "startDate")
    @Mapping(source = "createTripDto.endDate", target = "endDate")
    @Mapping(source = "createTripDto.country", target = "country")
    @Mapping(source = "createTripDto.city", target = "city")
    @Mapping(source = "createTripDto.published", target = "published")
    @Mapping(source = "creator", target = "creator")
    @Mapping(target = "previewUrl", ignore = true)
    @Mapping(target = "days", ignore = true)
    @Mapping(target = "accesses", ignore = true)
    @Mapping(target = "files", ignore = true)
    @Mapping(target = "todoLists", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    Trip toEntityFromCreate(CreateTripDto createTripDto, User creator);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(source = "title", target = "title")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "startDate", target = "startDate")
    @Mapping(source = "endDate", target = "endDate")
    @Mapping(source = "country", target = "country")
    @Mapping(source = "city", target = "city")
    @Mapping(source = "published", target = "published")
    @Mapping(target = "tripId", ignore = true)
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "previewUrl", ignore = true)
    @Mapping(target = "days", ignore = true)
    @Mapping(target = "accesses", ignore = true)
    @Mapping(target = "files", ignore = true)
    @Mapping(target = "todoLists", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void updateEntityFromUpdate(UpdateTripDto updateTripDto, @MappingTarget Trip trip);
} 