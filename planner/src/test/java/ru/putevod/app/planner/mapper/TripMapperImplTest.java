package ru.putevod.app.planner.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.putevod.app.planner.dto.CreateTripDto;
import ru.putevod.app.planner.dto.UpdateTripDto;
import ru.putevod.app.planner.model.Trip;
import ru.putevod.app.planner.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TripMapperImplTest {

    @InjectMocks
    private TripMapperImpl tripMapper;

    private User testUser;
    private Trip testTrip;
    private CreateTripDto createTripDto;
    private UpdateTripDto updateTripDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setUsername("Test User");

        testTrip = new Trip();
        testTrip.setTripId(1L);
        testTrip.setTitle("Original Title");
        testTrip.setDescription("Original Description");
        testTrip.setStartDate(LocalDate.now());
        testTrip.setEndDate(LocalDate.now().plusDays(3));
        testTrip.setCountry("Original Country");
        testTrip.setCity("Original City");
        testTrip.setPublished(false);
        testTrip.setPreviewUrl("original-url");
        testTrip.setCreator(testUser);

        createTripDto = new CreateTripDto();
        createTripDto.setTitle("New Trip");
        createTripDto.setDescription("New Description");
        createTripDto.setStartDate(LocalDate.now().plusDays(1));
        createTripDto.setEndDate(LocalDate.now().plusDays(5));
        createTripDto.setCountry("New Country");
        createTripDto.setCity("New City");
        createTripDto.setPublished(true);

        updateTripDto = new UpdateTripDto();
        updateTripDto.setTitle("Updated Title");
        updateTripDto.setDescription("Updated Description");
        updateTripDto.setStartDate(LocalDate.now().plusDays(2));
        updateTripDto.setEndDate(LocalDate.now().plusDays(6));
        updateTripDto.setCountry("Updated Country");
        updateTripDto.setCity("Updated City");
        updateTripDto.setPublished(true);
    }

    @Test
    @DisplayName("Should create trip entity from CreateTripDto successfully")
    void toEntityFromCreate_Success() {
        Trip result = tripMapper.toEntityFromCreate(createTripDto, testUser);

        assertNotNull(result);
        assertNull(result.getTripId());
        assertEquals(createTripDto.getTitle(), result.getTitle());
        assertEquals(createTripDto.getDescription(), result.getDescription());
        assertEquals(createTripDto.getStartDate(), result.getStartDate());
        assertEquals(createTripDto.getEndDate(), result.getEndDate());
        assertEquals(createTripDto.getCountry(), result.getCountry());
        assertEquals(createTripDto.getCity(), result.getCity());
        assertEquals(createTripDto.isPublished(), result.isPublished());
        assertEquals(testUser, result.getCreator());
        assertNull(result.getPreviewUrl());
        assertTrue(result.getDays().isEmpty());
        assertTrue(result.getAccesses().isEmpty());
        assertTrue(result.getFiles().isEmpty());
        assertTrue(result.getTodoLists().isEmpty());
        assertNull(result.getCreatedAt());
        assertNull(result.getUpdatedAt());
        assertFalse(result.isDeleted());
    }

    @Test
    @DisplayName("Should update trip entity from UpdateTripDto successfully")
    void updateEntityFromUpdate_Success() {
        tripMapper.updateEntityFromUpdate(updateTripDto, testTrip);

        assertEquals(1L, testTrip.getTripId());
        assertEquals(updateTripDto.getTitle(), testTrip.getTitle());
        assertEquals(updateTripDto.getDescription(), testTrip.getDescription());
        assertEquals(updateTripDto.getStartDate(), testTrip.getStartDate());
        assertEquals(updateTripDto.getEndDate(), testTrip.getEndDate());
        assertEquals(updateTripDto.getCountry(), testTrip.getCountry());
        assertEquals(updateTripDto.getCity(), testTrip.getCity());
        assertEquals(updateTripDto.isPublished(), testTrip.isPublished());
        assertEquals(testUser, testTrip.getCreator());
        assertEquals("original-url", testTrip.getPreviewUrl());
        assertTrue(testTrip.getDays().isEmpty());
        assertTrue(testTrip.getAccesses().isEmpty());
        assertTrue(testTrip.getFiles().isEmpty());
        assertTrue(testTrip.getTodoLists().isEmpty());
        assertNull(testTrip.getCreatedAt());
        assertNull(testTrip.getUpdatedAt());
        assertFalse(testTrip.isDeleted());
    }

    @Test
    @DisplayName("Should handle null values in UpdateTripDto")
    void updateEntityFromUpdate_WithNullValues() {
        UpdateTripDto partialUpdateDto = new UpdateTripDto();
        partialUpdateDto.setTitle("Updated Title");

        tripMapper.updateEntityFromUpdate(partialUpdateDto, testTrip);

        assertEquals(1L, testTrip.getTripId());
        assertEquals("Updated Title", testTrip.getTitle());
        assertEquals("Original Description", testTrip.getDescription());
        assertEquals(LocalDate.now(), testTrip.getStartDate());
        assertEquals(LocalDate.now().plusDays(3), testTrip.getEndDate());
        assertEquals("Original Country", testTrip.getCountry());
        assertEquals("Original City", testTrip.getCity());
        assertFalse(testTrip.isPublished());
        assertEquals("original-url", testTrip.getPreviewUrl());
        assertEquals(testUser, testTrip.getCreator());
        assertTrue(testTrip.getDays().isEmpty());
        assertTrue(testTrip.getAccesses().isEmpty());
        assertTrue(testTrip.getFiles().isEmpty());
        assertTrue(testTrip.getTodoLists().isEmpty());
        assertNull(testTrip.getCreatedAt());
        assertNull(testTrip.getUpdatedAt());
        assertFalse(testTrip.isDeleted());
    }

    @Test
    @DisplayName("Should handle null values in CreateTripDto")
    void toEntityFromCreate_WithNullValues() {
        CreateTripDto partialCreateDto = new CreateTripDto();
        partialCreateDto.setTitle("New Trip");

        Trip result = tripMapper.toEntityFromCreate(partialCreateDto, testUser);

        assertNotNull(result);
        assertNull(result.getTripId());
        assertEquals("New Trip", result.getTitle());
        assertNull(result.getDescription());
        assertNull(result.getStartDate());
        assertNull(result.getEndDate());
        assertNull(result.getCountry());
        assertNull(result.getCity());
        assertFalse(result.isPublished());
        assertEquals(testUser, result.getCreator());
        assertNull(result.getPreviewUrl());
        assertTrue(result.getDays().isEmpty());
        assertTrue(result.getAccesses().isEmpty());
        assertTrue(result.getFiles().isEmpty());
        assertTrue(result.getTodoLists().isEmpty());
        assertNull(result.getCreatedAt());
        assertNull(result.getUpdatedAt());
        assertFalse(result.isDeleted());
    }
} 