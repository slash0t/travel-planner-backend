package ru.putevod.app.planner.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.putevod.app.planner.dto.TripDayDto;
import ru.putevod.app.planner.model.Trip;
import ru.putevod.app.planner.model.TripDay;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class TripDayMapperTest {

    @Autowired
    private TripDayMapper tripDayMapper;

    @Test
    void testToDto() {
        TripDay tripDay = new TripDay();
        tripDay.setDayId(1L);
        tripDay.setDayNumber(1);
        tripDay.setDate(LocalDate.now());
        tripDay.setNote("Test note");
        tripDay.setCreatedAt(LocalDateTime.now());
        tripDay.setUpdatedAt(LocalDateTime.now());
        tripDay.setEvents(new ArrayList<>());

        Trip trip = new Trip();
        trip.setTripId(1L);
        tripDay.setTrip(trip);

        TripDayDto tripDayDto = tripDayMapper.toDto(tripDay);

        assertNotNull(tripDayDto);
        assertEquals(tripDay.getDayId(), tripDayDto.getId());
        assertEquals(tripDay.getDayNumber(), tripDayDto.getDayNumber());
        assertEquals(tripDay.getDate(), tripDayDto.getDate());
        assertEquals(tripDay.getNote(), tripDayDto.getNote());
        assertEquals(tripDay.getCreatedAt(), tripDayDto.getCreatedAt());
        assertEquals(tripDay.getUpdatedAt(), tripDayDto.getUpdatedAt());
        assertEquals(trip.getTripId(), tripDayDto.getTripId());
        assertNotNull(tripDayDto.getEvents());
        assertTrue(tripDayDto.getEvents().isEmpty());
    }

    @Test
    void testToDto_NullInput() {
        TripDayDto tripDayDto = tripDayMapper.toDto(null);

        assertNull(tripDayDto);
    }

    @Test
    void testToEntity() {
        TripDayDto tripDayDto = TripDayDto.builder()
                .id(1L)
                .dayNumber(1)
                .date(LocalDate.now())
                .note("Test note")
                .build();

        TripDay tripDay = tripDayMapper.toEntity(tripDayDto);

        assertNotNull(tripDay);
        assertEquals(tripDayDto.getId(), tripDay.getDayId());
        assertEquals(tripDayDto.getDayNumber(), tripDay.getDayNumber());
        assertEquals(tripDayDto.getDate(), tripDay.getDate());
        assertEquals(tripDayDto.getNote(), tripDay.getNote());
        assertNull(tripDay.getTrip());
        assertNull(tripDay.getEvents());
        assertNull(tripDay.getCreatedAt());
        assertNull(tripDay.getUpdatedAt());
    }

    @Test
    void testToEntity_NullInput() {
        TripDay tripDay = tripDayMapper.toEntity(null);

        assertNull(tripDay);
    }

    @Test
    void testUpdateEntityFromDto() {
        TripDay tripDay = new TripDay();
        tripDay.setDayId(1L);
        tripDay.setDayNumber(1);
        tripDay.setDate(LocalDate.now());
        tripDay.setNote("Old note");
        tripDay.setCreatedAt(LocalDateTime.now());
        tripDay.setUpdatedAt(LocalDateTime.now());

        Trip trip = new Trip();
        trip.setTripId(1L);
        tripDay.setTrip(trip);

        TripDayDto tripDayDto = TripDayDto.builder()
                .id(2L)
                .dayNumber(2)
                .date(LocalDate.now().plusDays(1))
                .note("New note")
                .build();

        tripDayMapper.updateEntityFromDto(tripDayDto, tripDay);

        assertEquals(1L, tripDay.getDayId());
        assertEquals(2, tripDay.getDayNumber());
        assertEquals(tripDayDto.getDate(), tripDay.getDate());
        assertEquals("New note", tripDay.getNote());
        assertEquals(trip, tripDay.getTrip());
        assertNotNull(tripDay.getCreatedAt());
    }

    @Test
    void testUpdateEntityFromDto_NullInput() {
        TripDay tripDay = new TripDay();
        tripDay.setDayId(1L);
        tripDay.setDayNumber(1);
        tripDay.setDate(LocalDate.now());
        tripDay.setNote("Test note");

        LocalDate originalDate = tripDay.getDate();
        String originalNote = tripDay.getNote();
        Integer originalDayNumber = tripDay.getDayNumber();

        tripDayMapper.updateEntityFromDto(null, tripDay);

        assertEquals(1L, tripDay.getDayId());
        assertEquals(originalDayNumber, tripDay.getDayNumber());
        assertEquals(originalDate, tripDay.getDate());
        assertEquals(originalNote, tripDay.getNote());
    }

    @Test
    void testFromDto() {
        TripDayDto tripDayDto = TripDayDto.builder()
                .id(1L)
                .dayNumber(1)
                .date(LocalDate.now())
                .note("Test note")
                .build();

        Trip trip = new Trip();
        trip.setTripId(1L);

        TripDay tripDay = tripDayMapper.fromDto(tripDayDto, trip);

        assertNotNull(tripDay);
        assertEquals(tripDayDto.getId(), tripDay.getDayId());
        assertEquals(tripDayDto.getDayNumber(), tripDay.getDayNumber());
        assertEquals(tripDayDto.getDate(), tripDay.getDate());
        assertEquals(tripDayDto.getNote(), tripDay.getNote());
        assertEquals(trip, tripDay.getTrip());
    }

    @Test
    void testFromDto_NullInput() {
        Trip trip = new Trip();
        trip.setTripId(1L);

        TripDay tripDay = tripDayMapper.fromDto(null, trip);

        assertNull(tripDay);
    }

    @Test
    void testFromDto_NullTrip() {
        TripDayDto tripDayDto = TripDayDto.builder()
                .id(1L)
                .dayNumber(1)
                .date(LocalDate.now())
                .note("Test note")
                .build();

        TripDay tripDay = tripDayMapper.fromDto(tripDayDto, null);

        assertNotNull(tripDay);
        assertEquals(tripDayDto.getId(), tripDay.getDayId());
        assertEquals(tripDayDto.getDayNumber(), tripDay.getDayNumber());
        assertEquals(tripDayDto.getDate(), tripDay.getDate());
        assertEquals(tripDayDto.getNote(), tripDay.getNote());
        assertNull(tripDay.getTrip());
    }
}
