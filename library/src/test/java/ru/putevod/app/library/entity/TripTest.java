package ru.putevod.app.library.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TripTest {

    private Trip trip;
    private LocalDateTime now;
    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        startDate = LocalDate.now();
        endDate = startDate.plusDays(5);

        trip = Trip.builder()
                .id(1L)
                .creatorId(100L)
                .title("Test Trip")
                .description("Test Description")
                .startDate(startDate)
                .endDate(endDate)
                .country("Test Country")
                .city("Test City")
                .isPublic(false)
                .isDeleted(false)
                .createdAt(now)
                .updatedAt(now)
                .days(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("Builder pattern creates valid Trip")
    void testBuilder() {
        assertNotNull(trip);
        assertEquals(1L, trip.getId());
        assertEquals(100L, trip.getCreatorId());
        assertEquals("Test Trip", trip.getTitle());
        assertEquals("Test Description", trip.getDescription());
        assertEquals(startDate, trip.getStartDate());
        assertEquals(endDate, trip.getEndDate());
        assertEquals("Test Country", trip.getCountry());
        assertEquals("Test City", trip.getCity());
        assertFalse(trip.getIsPublic());
        assertFalse(trip.getIsDeleted());
        assertEquals(now, trip.getCreatedAt());
        assertEquals(now, trip.getUpdatedAt());
        assertNotNull(trip.getDays());
        assertTrue(trip.getDays().isEmpty());
    }

    @Test
    @DisplayName("Setters and getters work correctly")
    void testSettersAndGetters() {
        trip.setId(2L);
        trip.setTitle("Updated Title");
        trip.setDescription("Updated Description");
        trip.setIsPublic(true);
        trip.setIsDeleted(true);

        assertEquals(2L, trip.getId());
        assertEquals("Updated Title", trip.getTitle());
        assertEquals("Updated Description", trip.getDescription());
        assertTrue(trip.getIsPublic());
        assertTrue(trip.getIsDeleted());
    }

    @Test
    @DisplayName("getDuration returns correct duration based on dates")
    void testGetDuration_WithDates() {
        assertEquals(6, trip.getDuration());

        trip.setEndDate(startDate);
        assertEquals(1, trip.getDuration());

        trip.setStartDate(null);
        trip.setEndDate(null);
        assertEquals(0, trip.getDuration());
    }

    @Test
    @DisplayName("getDuration returns correct duration based on days list")
    void testGetDuration_WithDays() {
        trip.setStartDate(null);
        trip.setEndDate(null);

        List<TripDay> days = new ArrayList<>();
        days.add(new TripDay());
        days.add(new TripDay());
        days.add(new TripDay());
        trip.setDays(days);

        assertEquals(3, trip.getDuration());
    }

    @Test
    @DisplayName("NoArgsConstructor creates valid Trip")
    void testNoArgsConstructor() {
        Trip emptyTrip = new Trip();
        assertNotNull(emptyTrip);
        assertNull(emptyTrip.getId());
        assertNull(emptyTrip.getCreatorId());
        assertNull(emptyTrip.getTitle());
        assertNull(emptyTrip.getDescription());
        assertNull(emptyTrip.getStartDate());
        assertNull(emptyTrip.getEndDate());
        assertNull(emptyTrip.getCountry());
        assertNull(emptyTrip.getCity());
        assertNull(emptyTrip.getIsPublic());
        assertNull(emptyTrip.getIsDeleted());
        assertNull(emptyTrip.getCreatedAt());
        assertNull(emptyTrip.getUpdatedAt());
        assertNotNull(emptyTrip.getDays());
        assertTrue(emptyTrip.getDays().isEmpty());
    }

    @Test
    @DisplayName("Trip with days list is properly initialized")
    void testTripWithDays() {
        List<TripDay> days = new ArrayList<>();
        TripDay day1 = new TripDay();
        day1.setId(1L);
        day1.setDayNumber(1);
        day1.setDate(startDate);

        TripDay day2 = new TripDay();
        day2.setId(2L);
        day2.setDayNumber(2);
        day2.setDate(startDate.plusDays(1));

        days.add(day1);
        days.add(day2);
        trip.setDays(days);

        assertNotNull(trip.getDays());
        assertEquals(2, trip.getDays().size());
        assertEquals(1, trip.getDays().get(0).getDayNumber());
        assertEquals(2, trip.getDays().get(1).getDayNumber());
    }
} 