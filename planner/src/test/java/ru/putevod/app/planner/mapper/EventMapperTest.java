package ru.putevod.app.planner.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.putevod.app.planner.dto.EventDto;
import ru.putevod.app.planner.dto.PlaceDto;
import ru.putevod.app.planner.model.Event;
import ru.putevod.app.planner.model.Place;
import ru.putevod.app.planner.model.TripDay;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class EventMapperTest {

    @Autowired
    private EventMapper eventMapper;

    @Test
    void testToDto() {
        Event event = new Event();
        event.setEventId(1L);
        event.setTitle("Test Event");
        event.setDescription("Test Description");
        event.setStartTime(LocalTime.of(10, 0));
        event.setEndTime(LocalTime.of(12, 0));
        event.setHasSpecificTime(true);
        event.setNotes("Test Notes");
        event.setOrderPosition(1);
        event.setCreatedAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());

        TripDay tripDay = new TripDay();
        tripDay.setDayId(1L);
        event.setDay(tripDay);

        Place place = new Place();
        place.setPlaceId(1L);
        place.setName("Test Place");
        event.setPlace(place);

        event.setFiles(new ArrayList<>());
        event.setReminders(new ArrayList<>());

        EventDto eventDto = eventMapper.toDto(event);

        assertNotNull(eventDto);
        assertEquals(event.getEventId(), eventDto.getId());
        assertEquals(event.getTitle(), eventDto.getTitle());
        assertEquals(event.getDescription(), eventDto.getDescription());
        assertEquals(event.getStartTime(), eventDto.getStartTime());
        assertEquals(event.getEndTime(), eventDto.getEndTime());
        assertEquals(event.isHasSpecificTime(), eventDto.isHasSpecificTime());
        assertEquals(event.getNotes(), eventDto.getNotes());
        assertEquals(event.getOrderPosition(), eventDto.getOrderPosition());
        assertEquals(event.getDay().getDayId(), eventDto.getDayId());
        assertNotNull(eventDto.getPlace());
        assertEquals(event.getPlace().getPlaceId(), eventDto.getPlace().getId());
        assertEquals(event.getPlace().getName(), eventDto.getPlace().getName());
        assertEquals(event.getCreatedAt(), eventDto.getCreatedAt());
        assertEquals(event.getUpdatedAt(), eventDto.getUpdatedAt());
    }

    @Test
    void testToEntity() {
        EventDto eventDto = EventDto.builder()
                .id(1L)
                .title("Test Event")
                .description("Test Description")
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(12, 0))
                .hasSpecificTime(true)
                .notes("Test Notes")
                .orderPosition(1)
                .dayId(1L)
                .place(PlaceDto.builder()
                        .id(1L)
                        .name("Test Place")
                        .build())
                .files(Collections.emptyList())
                .reminders(Collections.emptyList())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Event event = eventMapper.toEntity(eventDto);

        assertNotNull(event);
        assertEquals(eventDto.getId(), event.getEventId());
        assertEquals(eventDto.getTitle(), event.getTitle());
        assertEquals(eventDto.getDescription(), event.getDescription());
        assertEquals(eventDto.getStartTime(), event.getStartTime());
        assertEquals(eventDto.getEndTime(), event.getEndTime());
        assertEquals(eventDto.isHasSpecificTime(), event.isHasSpecificTime());
        assertEquals(eventDto.getNotes(), event.getNotes());
        assertEquals(eventDto.getOrderPosition(), event.getOrderPosition());
        
        assertNotNull(event.getPlace());
        assertEquals(eventDto.getPlace().getId(), event.getPlace().getPlaceId());
        assertEquals(eventDto.getPlace().getName(), event.getPlace().getName());
    }

    @Test
    void testUpdateEntityFromDto() {
        Event event = new Event();
        event.setEventId(1L);
        event.setTitle("Original Title");
        event.setDescription("Original Description");

        EventDto eventDto = EventDto.builder()
                .id(1L)
                .title("Updated Title")
                .description("Updated Description")
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(12, 0))
                .hasSpecificTime(true)
                .notes("Updated Notes")
                .orderPosition(2)
                .build();

        eventMapper.updateEntityFromDto(eventDto, event);

        assertEquals(eventDto.getTitle(), event.getTitle());
        assertEquals(eventDto.getDescription(), event.getDescription());
        assertEquals(eventDto.getStartTime(), event.getStartTime());
        assertEquals(eventDto.getEndTime(), event.getEndTime());
        assertEquals(eventDto.isHasSpecificTime(), event.isHasSpecificTime());
        assertEquals(eventDto.getNotes(), event.getNotes());
        assertEquals(eventDto.getOrderPosition(), event.getOrderPosition());
    }

    @Test
    void testFromDto() {
        EventDto eventDto = EventDto.builder()
                .id(1L)
                .title("Test Event")
                .description("Test Description")
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(12, 0))
                .hasSpecificTime(true)
                .notes("Test Notes")
                .orderPosition(1)
                .place(PlaceDto.builder()
                        .id(1L)
                        .name("Test Place")
                        .build())
                .build();

        TripDay day = new TripDay();
        day.setDayId(1L);

        Event event = eventMapper.fromDto(eventDto, day);

        assertNotNull(event);
        assertEquals(eventDto.getId(), event.getEventId());
        assertEquals(eventDto.getTitle(), event.getTitle());
        assertEquals(eventDto.getDescription(), event.getDescription());
        assertEquals(eventDto.getStartTime(), event.getStartTime());
        assertEquals(eventDto.getEndTime(), event.getEndTime());
        assertEquals(eventDto.isHasSpecificTime(), event.isHasSpecificTime());
        assertEquals(eventDto.getNotes(), event.getNotes());
        assertEquals(eventDto.getOrderPosition(), event.getOrderPosition());
        assertEquals(day, event.getDay());
        assertNotNull(event.getPlace());
        assertEquals(eventDto.getPlace().getId(), event.getPlace().getPlaceId());
        assertEquals(eventDto.getPlace().getName(), event.getPlace().getName());
    }

    @Test
    void testFromDto_NullInput() {
        TripDay day = new TripDay();
        day.setDayId(1L);

        Event event = eventMapper.fromDto(null, day);

        assertNull(event);
    }
}
