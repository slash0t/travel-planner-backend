package ru.putevod.app.planner.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.putevod.app.planner.dto.CreateEventDto;
import ru.putevod.app.planner.dto.EventDto;
import ru.putevod.app.planner.dto.PlaceDto;
import ru.putevod.app.planner.dto.UpdateEventDto;
import ru.putevod.app.planner.model.Event;
import ru.putevod.app.planner.model.Place;
import ru.putevod.app.planner.model.TripDay;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventMapperImplTest {

    @InjectMocks
    private EventMapperImpl eventMapper;

    @Mock
    private PlaceMapper placeMapper;

    private Event testEvent;
    private EventDto testEventDto;
    private CreateEventDto testCreateEventDto;
    private UpdateEventDto testUpdateEventDto;
    private TripDay testTripDay;
    private Place testPlace;
    private PlaceDto testPlaceDto;

    @BeforeEach
    void setUp() {
        testTripDay = new TripDay();
        testTripDay.setDayId(1L);

        testPlace = new Place();
        testPlace.setPlaceId(1L);
        testPlace.setName("Test Place");

        testPlaceDto = PlaceDto.builder()
                .id(1L)
                .name("Test Place")
                .build();

        testEvent = new Event();
        testEvent.setEventId(1L);
        testEvent.setTitle("Test Event");
        testEvent.setDescription("Test Description");
        testEvent.setStartTime(LocalTime.of(10, 0));
        testEvent.setEndTime(LocalTime.of(12, 0));
        testEvent.setHasSpecificTime(true);
        testEvent.setNotes("Test Notes");
        testEvent.setOrderPosition(1);
        testEvent.setDay(testTripDay);
        testEvent.setPlace(testPlace);
        testEvent.setFiles(new ArrayList<>());
        testEvent.setReminders(new ArrayList<>());
        testEvent.setCreatedAt(LocalDateTime.now());
        testEvent.setUpdatedAt(LocalDateTime.now());

        testEventDto = EventDto.builder()
                .id(1L)
                .title("Test Event")
                .description("Test Description")
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(12, 0))
                .hasSpecificTime(true)
                .notes("Test Notes")
                .orderPosition(1)
                .dayId(1L)
                .place(testPlaceDto)
                .files(new ArrayList<>())
                .reminders(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testCreateEventDto = new CreateEventDto();
        testCreateEventDto.setTitle("New Event");
        testCreateEventDto.setDescription("New Description");
        testCreateEventDto.setStartTime(LocalTime.of(14, 0));
        testCreateEventDto.setEndTime(LocalTime.of(16, 0));
        testCreateEventDto.setHasSpecificTime(true);
        testCreateEventDto.setNotes("New Notes");
        testCreateEventDto.setOrderPosition(2);

        testUpdateEventDto = new UpdateEventDto();
        testUpdateEventDto.setTitle("Updated Event");
        testUpdateEventDto.setDescription("Updated Description");
        testUpdateEventDto.setStartTime(LocalTime.of(15, 0));
        testUpdateEventDto.setEndTime(LocalTime.of(17, 0));
        testUpdateEventDto.setHasSpecificTime(true);
        testUpdateEventDto.setNotes("Updated Notes");
        testUpdateEventDto.setOrderPosition(3);
    }

    @Test
    @DisplayName("Should map Event to EventDto successfully")
    void toDto_Success() {
        when(placeMapper.toDto(any(Place.class))).thenReturn(testPlaceDto);

        EventDto result = eventMapper.toDto(testEvent);

        assertNotNull(result);
        assertEquals(testEvent.getEventId(), result.getId());
        assertEquals(testEvent.getTitle(), result.getTitle());
        assertEquals(testEvent.getDescription(), result.getDescription());
        assertEquals(testEvent.getStartTime(), result.getStartTime());
        assertEquals(testEvent.getEndTime(), result.getEndTime());
        assertEquals(testEvent.isHasSpecificTime(), result.isHasSpecificTime());
        assertEquals(testEvent.getNotes(), result.getNotes());
        assertEquals(testEvent.getOrderPosition(), result.getOrderPosition());
        assertEquals(testEvent.getDay().getDayId(), result.getDayId());
        assertNotNull(result.getPlace());
        assertEquals(testEvent.getPlace().getPlaceId(), result.getPlace().getId());
        assertEquals(testEvent.getPlace().getName(), result.getPlace().getName());
        assertEquals(testEvent.getCreatedAt(), result.getCreatedAt());
        assertEquals(testEvent.getUpdatedAt(), result.getUpdatedAt());
    }

    @Test
    @DisplayName("Should map EventDto to Event successfully")
    void toEntity_Success() {
        when(placeMapper.toEntity(any(PlaceDto.class))).thenReturn(testPlace);

        Event result = eventMapper.toEntity(testEventDto);

        assertNotNull(result);
        assertEquals(testEventDto.getId(), result.getEventId());
        assertEquals(testEventDto.getTitle(), result.getTitle());
        assertEquals(testEventDto.getDescription(), result.getDescription());
        assertEquals(testEventDto.getStartTime(), result.getStartTime());
        assertEquals(testEventDto.getEndTime(), result.getEndTime());
        assertEquals(testEventDto.isHasSpecificTime(), result.isHasSpecificTime());
        assertEquals(testEventDto.getNotes(), result.getNotes());
        assertEquals(testEventDto.getOrderPosition(), result.getOrderPosition());
        assertNotNull(result.getPlace());
        assertEquals(testEventDto.getPlace().getId(), result.getPlace().getPlaceId());
        assertEquals(testEventDto.getPlace().getName(), result.getPlace().getName());
    }

    @Test
    @DisplayName("Should update Event from EventDto successfully")
    void updateEntityFromDto_Success() {
        eventMapper.updateEntityFromDto(testEventDto, testEvent);

        assertEquals(testEventDto.getTitle(), testEvent.getTitle());
        assertEquals(testEventDto.getDescription(), testEvent.getDescription());
        assertEquals(testEventDto.getStartTime(), testEvent.getStartTime());
        assertEquals(testEventDto.getEndTime(), testEvent.getEndTime());
        assertEquals(testEventDto.isHasSpecificTime(), testEvent.isHasSpecificTime());
        assertEquals(testEventDto.getNotes(), testEvent.getNotes());
        assertEquals(testEventDto.getOrderPosition(), testEvent.getOrderPosition());
    }

    @Test
    @DisplayName("Should create Event from CreateEventDto successfully")
    void toEntityFromCreate_Success() {
        Event result = eventMapper.toEntityFromCreate(testCreateEventDto, testTripDay);

        assertNotNull(result);
        assertNull(result.getEventId());
        assertEquals(testCreateEventDto.getTitle(), result.getTitle());
        assertEquals(testCreateEventDto.getDescription(), result.getDescription());
        assertEquals(testCreateEventDto.getStartTime(), result.getStartTime());
        assertEquals(testCreateEventDto.getEndTime(), result.getEndTime());
        assertEquals(testCreateEventDto.getHasSpecificTime(), result.isHasSpecificTime());
        assertEquals(testCreateEventDto.getNotes(), result.getNotes());
        assertEquals(testCreateEventDto.getOrderPosition(), result.getOrderPosition());
        assertEquals(testTripDay, result.getDay());
        assertNull(result.getPlace());
        assertTrue(result.getFiles().isEmpty());
        assertTrue(result.getReminders().isEmpty());
        assertNull(result.getCreatedAt());
        assertNull(result.getUpdatedAt());
    }

    @Test
    @DisplayName("Should update Event from UpdateEventDto successfully")
    void updateEntityFromUpdate_Success() {
        eventMapper.updateEntityFromUpdate(testUpdateEventDto, testEvent);

        assertEquals(testUpdateEventDto.getTitle(), testEvent.getTitle());
        assertEquals(testUpdateEventDto.getDescription(), testEvent.getDescription());
        assertEquals(testUpdateEventDto.getStartTime(), testEvent.getStartTime());
        assertEquals(testUpdateEventDto.getEndTime(), testEvent.getEndTime());
        assertEquals(testUpdateEventDto.isHasSpecificTime(), testEvent.isHasSpecificTime());
        assertEquals(testUpdateEventDto.getNotes(), testEvent.getNotes());
        assertEquals(testUpdateEventDto.getOrderPosition(), testEvent.getOrderPosition());
    }

    @Test
    @DisplayName("Should handle null values in CreateEventDto")
    void toEntityFromCreate_WithNullValues() {
        CreateEventDto partialCreateDto = new CreateEventDto();
        partialCreateDto.setTitle("New Event");

        Event result = eventMapper.toEntityFromCreate(partialCreateDto, testTripDay);

        assertNotNull(result);
        assertNull(result.getEventId());
        assertEquals("New Event", result.getTitle());
        assertNull(result.getDescription());
        assertNull(result.getStartTime());
        assertNull(result.getEndTime());
        assertFalse(result.isHasSpecificTime());
        assertNull(result.getNotes());
        assertNull(result.getOrderPosition());
        assertEquals(testTripDay, result.getDay());
        assertNull(result.getPlace());
        assertTrue(result.getFiles().isEmpty());
        assertTrue(result.getReminders().isEmpty());
        assertNull(result.getCreatedAt());
        assertNull(result.getUpdatedAt());
    }

    @Test
    @DisplayName("Should create Event from EventDto with TripDay")
    void fromDto_Success() {
        when(placeMapper.toEntity(any(PlaceDto.class))).thenReturn(testPlace);

        Event result = eventMapper.fromDto(testEventDto, testTripDay);

        assertNotNull(result);
        assertEquals(testEventDto.getId(), result.getEventId());
        assertEquals(testEventDto.getTitle(), result.getTitle());
        assertEquals(testEventDto.getDescription(), result.getDescription());
        assertEquals(testEventDto.getStartTime(), result.getStartTime());
        assertEquals(testEventDto.getEndTime(), result.getEndTime());
        assertEquals(testEventDto.isHasSpecificTime(), result.isHasSpecificTime());
        assertEquals(testEventDto.getNotes(), result.getNotes());
        assertEquals(testEventDto.getOrderPosition(), result.getOrderPosition());
        assertEquals(testTripDay, result.getDay());
        assertNotNull(result.getPlace());
        assertEquals(testEventDto.getPlace().getId(), result.getPlace().getPlaceId());
        assertEquals(testEventDto.getPlace().getName(), result.getPlace().getName());
    }

    @Test
    @DisplayName("Should return null when EventDto is null")
    void fromDto_NullInput() {
        Event result = eventMapper.fromDto(null, testTripDay);
        assertNull(result);
    }
} 