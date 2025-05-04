package ru.putevod.app.planner.mapper;

import org.mapstruct.*;
import ru.putevod.app.planner.dto.TripAccessDto;
import ru.putevod.app.planner.model.Trip;
import ru.putevod.app.planner.model.TripAccess;
import ru.putevod.app.planner.model.User;

@Mapper(componentModel = "spring", 
        uses = {UserMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TripAccessMapper {
    
    @Mapping(source = "accessId", target = "id")
    @Mapping(source = "trip.tripId", target = "tripId")
    TripAccessDto toDto(TripAccess tripAccess);
    
    @Mapping(source = "id", target = "accessId")
    @Mapping(target = "trip", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    TripAccess toEntity(TripAccessDto tripAccessDto);
    
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