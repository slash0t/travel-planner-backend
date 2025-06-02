package ru.putevod.app.planner.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.putevod.app.planner.dto.CreateEventDto;
import ru.putevod.app.planner.dto.UpdateEventDto;
import ru.putevod.app.planner.dto.CreateEventReminderDto;
import ru.putevod.app.planner.dto.EventDto;
import ru.putevod.app.planner.dto.EventReminderDto;
import ru.putevod.app.planner.service.EventService;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class EventControllerTest {

    @Mock
    private EventService eventService;

    @InjectMocks
    private EventController eventController;

    private Long userId;
    private Long tripId;
    private Long dayId;
    private Long eventId;
    private Long reminderId;
    private CreateEventDto mockCreateEventDto;
    private UpdateEventDto mockUpdateEventDto;
    private CreateEventReminderDto mockCreateEventReminderDto;
    private EventDto mockEventDto;
    private EventReminderDto mockEventReminderDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        userId = 1L;
        tripId = 1L;
        dayId = 1L;
        eventId = 1L;
        reminderId = 1L;

        mockCreateEventDto = CreateEventDto.builder()
                .title("Test Event")
                .description("Test Description")
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .hasSpecificTime(true)
                .notes("Test Notes")
                .orderPosition(1)
                .build();

        mockUpdateEventDto = UpdateEventDto.builder()
                .title("Updated Event")
                .description("Updated Description")
                .startTime(LocalTime.of(11, 0))
                .endTime(LocalTime.of(12, 0))
                .hasSpecificTime(true)
                .notes("Updated Notes")
                .orderPosition(1)
                .build();

        mockCreateEventReminderDto = CreateEventReminderDto.builder()
                .remindAt(LocalDateTime.now().plusHours(1))
                .minutesBefore(60)
                .build();

        mockEventDto = EventDto.builder()
                .id(eventId)
                .dayId(dayId)
                .title("Test Event")
                .description("Test Description")
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .hasSpecificTime(true)
                .notes("Test Notes")
                .orderPosition(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        mockEventReminderDto = EventReminderDto.builder()
                .id(reminderId)
                .eventId(eventId)
                .remindAt(LocalDateTime.now().plusHours(1))
                .minutesBefore(60)
                .build();
    }

    @Test
    void createEvent_ShouldReturnCreatedEvent() {
        when(eventService.createEvent(eq(userId), eq(tripId), eq(dayId), any(CreateEventDto.class)))
                .thenReturn(mockEventDto);

        ResponseEntity<EventDto> response = eventController.createEvent(userId, tripId, dayId, mockCreateEventDto);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockEventDto.getId(), response.getBody().getId());
        assertEquals(mockEventDto.getTitle(), response.getBody().getTitle());
        verify(eventService).createEvent(eq(userId), eq(tripId), eq(dayId), any(CreateEventDto.class));
    }

    @Test
    void getDayEvents_ShouldReturnEvents() {
        List<EventDto> events = Arrays.asList(mockEventDto);
        when(eventService.getDayEvents(userId, tripId, dayId))
                .thenReturn(events);

        ResponseEntity<List<EventDto>> response = eventController.getDayEvents(userId, tripId, dayId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(mockEventDto.getId(), response.getBody().get(0).getId());
        verify(eventService).getDayEvents(userId, tripId, dayId);
    }

    @Test
    void getEvent_ShouldReturnEvent() {
        when(eventService.getEvent(userId, tripId, dayId, eventId))
                .thenReturn(mockEventDto);

        ResponseEntity<EventDto> response = eventController.getEvent(userId, tripId, dayId, eventId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockEventDto.getId(), response.getBody().getId());
        verify(eventService).getEvent(userId, tripId, dayId, eventId);
    }

    @Test
    void updateEvent_ShouldReturnUpdatedEvent() {
        when(eventService.updateEvent(eq(userId), eq(tripId), eq(dayId), eq(eventId), any(UpdateEventDto.class)))
                .thenReturn(mockEventDto);

        ResponseEntity<EventDto> response = eventController.updateEvent(userId, tripId, dayId, eventId, mockUpdateEventDto);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockEventDto.getId(), response.getBody().getId());
        verify(eventService).updateEvent(eq(userId), eq(tripId), eq(dayId), eq(eventId), any(UpdateEventDto.class));
    }

    @Test
    void deleteEvent_ShouldReturnNoContent() {
        doNothing().when(eventService).deleteEvent(userId, tripId, dayId, eventId);

        ResponseEntity<Void> response = eventController.deleteEvent(userId, tripId, dayId, eventId);

        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(eventService).deleteEvent(userId, tripId, dayId, eventId);
    }

    @Test
    void addEventReminder_ShouldReturnCreatedReminder() {
        when(eventService.addEventReminder(eq(userId), eq(eventId), any(CreateEventReminderDto.class)))
                .thenReturn(mockEventReminderDto);

        ResponseEntity<EventReminderDto> response = eventController.addEventReminder(userId, eventId, mockCreateEventReminderDto);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockEventReminderDto.getId(), response.getBody().getId());
        assertEquals(mockEventReminderDto.getEventId(), response.getBody().getEventId());
        verify(eventService).addEventReminder(eq(userId), eq(eventId), any(CreateEventReminderDto.class));
    }

    @Test
    void getEventReminders_ShouldReturnReminders() {
        List<EventReminderDto> reminders = Arrays.asList(mockEventReminderDto);
        when(eventService.getEventReminders(userId, eventId))
                .thenReturn(reminders);

        ResponseEntity<List<EventReminderDto>> response = eventController.getEventReminders(userId, eventId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(mockEventReminderDto.getId(), response.getBody().get(0).getId());
        verify(eventService).getEventReminders(userId, eventId);
    }

    @Test
    void deleteEventReminder_ShouldReturnNoContent() {
        doNothing().when(eventService).deleteEventReminder(userId, eventId, reminderId);

        ResponseEntity<Void> response = eventController.deleteEventReminder(userId, eventId, reminderId);

        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(eventService).deleteEventReminder(userId, eventId, reminderId);
    }
} 