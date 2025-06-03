package ru.putevod.app.planner.mapper;

import org.mapstruct.*;
import ru.putevod.app.planner.dto.CreateTripAccessDto;
import ru.putevod.app.planner.dto.TripAccessDto;
import ru.putevod.app.planner.model.TripAccess;
import ru.putevod.app.planner.model.User;
import ru.putevod.app.planner.model.Trip;

@Mapper(
        config = MapstructConfig.class,
        uses = {UserMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface TripAccessMapper {

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(source = "accessId", target = "id")
    @Mapping(source = "trip.tripId", target = "tripId")
    TripAccessDto toDto(TripAccess tripAccess);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(source = "id", target = "accessId")
    @Mapping(target = "trip", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    TripAccess toEntity(TripAccessDto tripAccessDto);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(target = "accessId", ignore = true)
    @Mapping(source = "createTripAccessDto.accessLevel", target = "accessLevel")
    @Mapping(source = "user", target = "user")
    @Mapping(source = "trip", target = "trip")
    @Mapping(target = "invitationStatus", constant = "pending")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    TripAccess toEntityFromCreate(CreateTripAccessDto createTripAccessDto, User user, Trip trip);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(source = "accessLevel", target = "accessLevel")
    @Mapping(target = "accessId", ignore = true)
    @Mapping(target = "trip", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "invitationStatus", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(TripAccessDto tripAccessDto, @MappingTarget TripAccess tripAccess);

    default TripAccess fromDto(TripAccessDto tripAccessDto, Trip trip, User user) {
        if (tripAccessDto == null) {
            return null;
        }

        TripAccess tripAccess = toEntity(tripAccessDto);
        tripAccess.setTrip(trip);
        tripAccess.setUser(user);

        return tripAccess;
    }
} 