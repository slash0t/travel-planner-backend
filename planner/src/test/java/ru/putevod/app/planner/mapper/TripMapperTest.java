package ru.putevod.app.planner.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.putevod.app.planner.dto.TripDto;
import ru.putevod.app.planner.dto.UserDto;
import ru.putevod.app.planner.model.Trip;
import ru.putevod.app.planner.model.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class TripMapperTest {

    @Autowired
    private TripMapper tripMapper;

    @Test
    void testToDto() {
        Trip trip = new Trip();
        trip.setTripId(1L);
        trip.setTitle("Test Trip");
        trip.setDescription("Test Description");
        trip.setStartDate(LocalDate.now());
        trip.setEndDate(LocalDate.now().plusDays(5));
        trip.setCountry("Test Country");
        trip.setCity("Test City");
        trip.setPreviewUrl("test-url");
        trip.setPublished(true);
        trip.setCreatedAt(LocalDateTime.now());
        trip.setUpdatedAt(LocalDateTime.now());
        User user = new User();
        user.setUserId(1L);
        user.setUsername("Test User");
        trip.setCreator(user);
        trip.setDays(new ArrayList<>());
        trip.setTodoLists(new ArrayList<>());
        trip.setFiles(new ArrayList<>());

        TripDto tripDto = tripMapper.toDto(trip);

        assertNotNull(tripDto);
        assertEquals(trip.getTripId(), tripDto.getId());
        assertEquals(trip.getTitle(), tripDto.getTitle());
        assertEquals(trip.getDescription(), tripDto.getDescription());
        assertEquals(trip.getStartDate(), tripDto.getStartDate());
        assertEquals(trip.getEndDate(), tripDto.getEndDate());
        assertEquals(trip.getCountry(), tripDto.getCountry());
        assertEquals(trip.getCity(), tripDto.getCity());
        assertEquals(trip.getPreviewUrl(), tripDto.getPreviewUrl());
        assertEquals(trip.isPublished(), tripDto.isPublished());
        assertEquals(trip.getCreatedAt(), tripDto.getCreatedAt());
        assertEquals(trip.getUpdatedAt(), tripDto.getUpdatedAt());
        assertNotNull(tripDto.getCreator());
        assertEquals(trip.getCreator().getUserId(), tripDto.getCreator().getId());
        assertEquals(trip.getCreator().getUsername(), tripDto.getCreator().getUsername());
    }

    @Test
    void testToEntity() {
        TripDto tripDto = TripDto.builder()
                .id(1L)
                .title("Test Trip")
                .description("Test Description")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(5))
                .country("Test Country")
                .city("Test City")
                .previewUrl("test-url")
                .published(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .days(new ArrayList<>())
                .todoLists(new ArrayList<>())
                .files(new ArrayList<>())
                .creator(UserDto.builder().id(1L).username("Test User").build())
                .build();

        Trip trip = tripMapper.toEntity(tripDto);

        assertNotNull(trip);
        assertEquals(tripDto.getId(), trip.getTripId());
        assertEquals(tripDto.getTitle(), trip.getTitle());
        assertEquals(tripDto.getDescription(), trip.getDescription());
        assertEquals(tripDto.getStartDate(), trip.getStartDate());
        assertEquals(tripDto.getEndDate(), trip.getEndDate());
        assertEquals(tripDto.getCountry(), trip.getCountry());
        assertEquals(tripDto.getCity(), trip.getCity());
        assertEquals(tripDto.getPreviewUrl(), trip.getPreviewUrl());
        assertEquals(tripDto.isPublished(), trip.isPublished());
    }

    @Test
    void testComputeDerivedFields() {
        TripDto tripDto = TripDto.builder()
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(4))
                .build();
        Trip trip = new Trip();
        trip.setStartDate(LocalDate.now().minusDays(2));
        trip.setEndDate(LocalDate.now().plusDays(2));

        tripMapper.computeDerivedFields(tripDto, trip);

        assertEquals("ongoing", tripDto.getStatus());
        assertEquals(5, tripDto.getTotalDays());
    }

    @Test
    void testUpdateEntityFromDto() {
        TripDto tripDto = new TripDto();
        tripDto.setTitle("Updated Title");
        tripDto.setDescription("Updated Description");
        tripDto.setStartDate(LocalDate.now().plusDays(1));
        tripDto.setEndDate(LocalDate.now().plusDays(5));
        tripDto.setCountry("Updated Country");
        tripDto.setCity("Updated City");
        tripDto.setPublished(true);
        tripDto.setPreviewUrl("updated-url");

        Trip trip = new Trip();
        trip.setTripId(1L);
        trip.setTitle("Original Title");
        trip.setDescription("Original Description");
        trip.setStartDate(LocalDate.now());
        trip.setEndDate(LocalDate.now().plusDays(3));
        trip.setCountry("Original Country");
        trip.setCity("Original City");
        trip.setPublished(false);
        trip.setPreviewUrl("original-url");
        trip.setCreatedAt(LocalDateTime.now());
        trip.setUpdatedAt(LocalDateTime.now());
        trip.setDeleted(false);

        User user = new User();
        user.setUserId(1L);
        user.setUsername("Test User");
        trip.setCreator(user);

        tripMapper.updateEntityFromDto(tripDto, trip);

        assertEquals(tripDto.getTitle(), trip.getTitle());
        assertEquals(tripDto.getDescription(), trip.getDescription());
        assertEquals(tripDto.getStartDate(), trip.getStartDate());
        assertEquals(tripDto.getEndDate(), trip.getEndDate());
        assertEquals(tripDto.getCountry(), trip.getCountry());
        assertEquals(tripDto.getCity(), trip.getCity());
        assertEquals(tripDto.isPublished(), trip.isPublished());
        assertEquals(tripDto.getPreviewUrl(), trip.getPreviewUrl());

        assertEquals(1L, trip.getTripId());
        assertEquals(user, trip.getCreator());
        assertNotNull(trip.getCreatedAt());
        assertNotNull(trip.getUpdatedAt());
        assertFalse(trip.isDeleted());
    }

    @Test
    void testUpdateEntityFromDto_WithNullValues() {
        TripDto tripDto = new TripDto();
        tripDto.setTitle("Updated Title");

        Trip trip = new Trip();
        trip.setTripId(1L);
        trip.setTitle("Original Title");
        trip.setDescription("Original Description");
        trip.setStartDate(LocalDate.now());
        trip.setEndDate(LocalDate.now().plusDays(3));
        trip.setCountry("Original Country");
        trip.setCity("Original City");
        trip.setPublished(false);
        trip.setPreviewUrl("original-url");
        trip.setCreatedAt(LocalDateTime.now());
        trip.setUpdatedAt(LocalDateTime.now());
        trip.setDeleted(false);

        User user = new User();
        user.setUserId(1L);
        user.setUsername("Test User");
        trip.setCreator(user);

        tripMapper.updateEntityFromDto(tripDto, trip);

        assertEquals(tripDto.getTitle(), trip.getTitle());
        assertEquals("Original Description", trip.getDescription());
        assertEquals(LocalDate.now(), trip.getStartDate());
        assertEquals(LocalDate.now().plusDays(3), trip.getEndDate());
        assertEquals("Original Country", trip.getCountry());
        assertEquals("Original City", trip.getCity());
        assertFalse(trip.isPublished());
        assertEquals("original-url", trip.getPreviewUrl());

        assertEquals(1L, trip.getTripId());
        assertEquals(user, trip.getCreator());
        assertNotNull(trip.getCreatedAt());
        assertNotNull(trip.getUpdatedAt());
        assertFalse(trip.isDeleted());
    }
}
