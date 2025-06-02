package ru.putevod.app.planner.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.putevod.app.planner.dto.TripAccessDto;
import ru.putevod.app.planner.model.Trip;
import ru.putevod.app.planner.model.TripAccess;
import ru.putevod.app.planner.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class TripAccessMapperTest {

    @Autowired
    private TripAccessMapper tripAccessMapper;

    @Test
    void toDto_ShouldMapCorrectly() {
        User user = new User();
        user.setUserId(1L);
        user.setUsername("testuser");

        Trip trip = new Trip();
        trip.setTripId(1L);

        TripAccess tripAccess = new TripAccess();
        tripAccess.setAccessId(1L);
        tripAccess.setTrip(trip);
        tripAccess.setUser(user);
        tripAccess.setAccessLevel("admin");
        tripAccess.setInvitationStatus("accepted");
        tripAccess.setCreatedAt(LocalDateTime.now());
        tripAccess.setUpdatedAt(LocalDateTime.now());

        TripAccessDto result = tripAccessMapper.toDto(tripAccess);

        assertNotNull(result);
        assertEquals(tripAccess.getAccessId(), result.getId());
        assertEquals(tripAccess.getTrip().getTripId(), result.getTripId());
        assertNotNull(result.getUser());
        assertEquals(tripAccess.getUser().getUserId(), result.getUser().getId());
        assertEquals(tripAccess.getAccessLevel(), result.getAccessLevel());
        assertEquals(tripAccess.getInvitationStatus(), result.getInvitationStatus());
        assertEquals(tripAccess.getCreatedAt(), result.getCreatedAt());
        assertEquals(tripAccess.getUpdatedAt(), result.getUpdatedAt());
    }

    @Test
    void toEntity_ShouldMapCorrectly() {
        TripAccessDto tripAccessDto = new TripAccessDto();
        tripAccessDto.setId(1L);
        tripAccessDto.setTripId(1L);
        tripAccessDto.setAccessLevel("admin");
        tripAccessDto.setInvitationStatus("accepted");
        tripAccessDto.setCreatedAt(LocalDateTime.now());
        tripAccessDto.setUpdatedAt(LocalDateTime.now());

        TripAccess result = tripAccessMapper.toEntity(tripAccessDto);

        assertNotNull(result);
        assertEquals(tripAccessDto.getId(), result.getAccessId());
        assertEquals(tripAccessDto.getAccessLevel(), result.getAccessLevel());
        assertEquals(tripAccessDto.getInvitationStatus(), result.getInvitationStatus());
    }

    @Test
    void fromDto_ShouldMapCorrectly() {
        TripAccessDto tripAccessDto = new TripAccessDto();
        tripAccessDto.setId(1L);
        tripAccessDto.setTripId(1L);
        tripAccessDto.setAccessLevel("admin");
        tripAccessDto.setInvitationStatus("accepted");

        Trip trip = new Trip();
        trip.setTripId(1L);

        User user = new User();
        user.setUserId(1L);
        user.setUsername("testuser");

        TripAccess result = tripAccessMapper.fromDto(tripAccessDto, trip, user);

        assertNotNull(result);
        assertEquals(tripAccessDto.getId(), result.getAccessId());
        assertEquals(tripAccessDto.getAccessLevel(), result.getAccessLevel());
        assertEquals(tripAccessDto.getInvitationStatus(), result.getInvitationStatus());
        assertEquals(trip, result.getTrip());
        assertEquals(user, result.getUser());
    }
} 