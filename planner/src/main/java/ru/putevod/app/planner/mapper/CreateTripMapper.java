package ru.putevod.app.planner.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.putevod.app.planner.dto.CreateTripDto;
import ru.putevod.app.planner.dto.TripDto;
import ru.putevod.app.planner.model.Trip;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CreateTripMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "days", ignore = true)
    @Mapping(target = "access", ignore = true)
    @Mapping(target = "files", ignore = true)
    @Mapping(target = "todoLists", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "totalDays", ignore = true)
    TripDto toTripDto(CreateTripDto createTripDto);

    @Mapping(target = "tripId", ignore = true)
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "days", ignore = true)
    @Mapping(target = "accesses", ignore = true)
    @Mapping(target = "files", ignore = true)
    @Mapping(target = "todoLists", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", constant = "false")
    Trip toEntity(CreateTripDto createTripDto);
} 