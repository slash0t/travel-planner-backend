package ru.putevod.app.planner.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.putevod.app.planner.dto.CreateEventDto;
import ru.putevod.app.planner.dto.EventDto;
import ru.putevod.app.planner.dto.EventReminderDto;
import ru.putevod.app.planner.dto.PlaceDto;
import ru.putevod.app.planner.mapper.CreateEventMapper;
import ru.putevod.app.planner.mapper.EventMapper;
import ru.putevod.app.planner.mapper.EventReminderMapper;
import ru.putevod.app.planner.mapper.PlaceMapper;
import ru.putevod.app.planner.model.*;
import ru.putevod.app.planner.repository.EventReminderRepository;
import ru.putevod.app.planner.repository.EventRepository;
import ru.putevod.app.planner.repository.PlaceRepository;
import ru.putevod.app.planner.repository.TripDayRepository;
import ru.putevod.app.planner.service.NotificationService;
import ru.putevod.app.planner.service.TripService;
import ru.putevod.app.planner.service.UserService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;
    @Mock
    private TripDayRepository tripDayRepository;
    @Mock
    private EventReminderRepository eventReminderRepository;
    @Mock
    private PlaceRepository placeRepository;
    @Mock
    private TripService tripService;
    @Mock
    private UserService userService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private EventMapper eventMapper;
    @Mock
    private CreateEventMapper createEventMapper;
    @Mock
    private PlaceMapper placeMapper;
    @Mock
    private EventReminderMapper eventReminderMapper;

    @InjectMocks
    private EventServiceImpl eventService;

    private User user;
    private Trip trip;
    private TripDay tripDay;
    private Event event;
    private EventDto eventDto;
    private CreateEventDto createEventDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(1L);

        trip = new Trip();
        trip.setTripId(1L);

        tripDay = new TripDay();
        tripDay.setDayId(1L);
        tripDay.setTrip(trip);
        tripDay.setDate(LocalDate.now());

        event = new Event();
        event.setEventId(1L);
        event.setDay(tripDay);
        event.setStartTime(LocalTime.now());
        event.setHasSpecificTime(true);

        eventDto = new EventDto();
        eventDto.setId(1L);

        createEventDto = new CreateEventDto();
        createEventDto.setOrderPosition(1);
    }

    @Test
    void createEvent_Success() {
        doReturn(trip).when(tripService).getTripEntityWithAccessCheck(anyLong(), anyLong());
        doReturn(user).when(userService).getUserEntityById(anyLong());
        doReturn(true).when(tripService).hasAccessToTrip(any(User.class), any(Trip.class), eq("admin"), eq("write"));
        doReturn(Optional.of(tripDay)).when(tripDayRepository).findByTripAndDayId(any(), any());
        doReturn(List.of()).when(eventRepository).findByDayOrderByOrderPositionAsc(any());
        doReturn(event).when(createEventMapper).fromDto(any(), any());
        doReturn(event).when(eventRepository).save(any());
        doReturn(eventDto).when(eventMapper).toDto(any());

        EventDto result = eventService.createEvent(1L, 1L, 1L, createEventDto);

        assertNotNull(result);
        verify(eventRepository).save(any());
    }

    @Test
    void updateEvent_Success() {
        doReturn(trip).when(tripService).getTripEntityWithAccessCheck(anyLong(), anyLong());
        doReturn(user).when(userService).getUserEntityById(anyLong());
        doReturn(true).when(tripService).hasAccessToTrip(any(User.class), any(Trip.class), eq("admin"), eq("write"));
        doReturn(Optional.of(tripDay)).when(tripDayRepository).findByTripAndDayId(any(), any());
        doReturn(Optional.of(event)).when(eventRepository).findById(any());
        doReturn(event).when(eventRepository).save(any());
        doReturn(eventDto).when(eventMapper).toDto(any());

        EventDto result = eventService.updateEvent(1L, 1L, 1L, 1L, eventDto);

        assertNotNull(result);
        verify(eventRepository).save(any());
    }

    @Test
    void getEvent_Success() {
        doReturn(trip).when(tripService).getTripEntityWithAccessCheck(anyLong(), anyLong());
        doReturn(user).when(userService).getUserEntityById(anyLong());
        doReturn(true).when(tripService).hasAccessToTrip(any(User.class), any(Trip.class), eq("admin"), eq("read"), eq("write"));
        doReturn(Optional.of(tripDay)).when(tripDayRepository).findByTripAndDayId(any(), any());
        doReturn(Optional.of(event)).when(eventRepository).findById(any());
        doReturn(eventDto).when(eventMapper).toDto(any());

        EventDto result = eventService.getEvent(1L, 1L, 1L, 1L);

        assertNotNull(result);
    }

    @Test
    void getDayEvents_Success() {
        doReturn(trip).when(tripService).getTripEntityWithAccessCheck(anyLong(), anyLong());
        doReturn(user).when(userService).getUserEntityById(anyLong());
        doReturn(true).when(tripService).hasAccessToTrip(any(User.class), any(Trip.class), eq("admin"), eq("read"), eq("write"));
        doReturn(Optional.of(tripDay)).when(tripDayRepository).findByTripAndDayId(any(), any());
        doReturn(List.of(event)).when(eventRepository).findByDayOrderByOrderPositionAsc(any());
        doReturn(eventDto).when(eventMapper).toDto(any());

        List<EventDto> result = eventService.getDayEvents(1L, 1L, 1L);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void getTripEvents_Success() {
        doReturn(trip).when(tripService).getTripEntityWithAccessCheck(anyLong(), anyLong());
        doReturn(user).when(userService).getUserEntityById(anyLong());
        doReturn(true).when(tripService).hasAccessToTrip(any(User.class), any(Trip.class), eq("admin"), eq("read"), eq("write"));
        doReturn(List.of(event)).when(eventRepository).findAllByTrip(any());
        doReturn(eventDto).when(eventMapper).toDto(any());

        List<EventDto> result = eventService.getTripEvents(1L, 1L);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void deleteEvent_Success() {
        doReturn(trip).when(tripService).getTripEntityWithAccessCheck(anyLong(), anyLong());
        doReturn(user).when(userService).getUserEntityById(anyLong());
        doReturn(true).when(tripService).hasAccessToTrip(any(User.class), any(Trip.class), eq("admin"), eq("write"));
        doReturn(Optional.of(tripDay)).when(tripDayRepository).findByTripAndDayId(any(), any());
        doReturn(Optional.of(event)).when(eventRepository).findById(any());

        eventService.deleteEvent(1L, 1L, 1L, 1L);

        verify(eventRepository).delete(any());
    }

    @Test
    void getUpcomingEvents_Success() {
        doReturn(user).when(userService).getUserEntityById(anyLong());
        doReturn(List.of(event)).when(eventRepository).findUpcomingEventsForUser(any(), anyInt());
        doReturn(eventDto).when(eventMapper).toDto(any());

        List<EventDto> result = eventService.getUpcomingEvents(1L, 7);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void addEventReminder_Success() {
        EventReminderDto reminderDto = new EventReminderDto();
        reminderDto.setMinutesBefore(30);
        EventReminder reminder = new EventReminder();
        EventReminderDto reminderDtoResult = new EventReminderDto();

        doReturn(user).when(userService).getUserEntityById(anyLong());
        doReturn(Optional.of(event)).when(eventRepository).findById(any());
        doReturn(true).when(tripService).hasAccessToTrip(any(User.class), any(Trip.class), eq("admin"), eq("read"), eq("write"));
        doReturn(reminder).when(eventReminderMapper).fromDto(any(), any(), any());
        doReturn(reminder).when(eventReminderRepository).save(any());
        doReturn(reminderDtoResult).when(eventReminderMapper).toDto(any());

        EventReminderDto result = eventService.addEventReminder(1L, 1L, reminderDto);

        assertNotNull(result);
        verify(eventReminderRepository).save(any());
    }
} 