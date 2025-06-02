package ru.putevod.app.planner.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import ru.putevod.app.planner.dto.CreateEventDto;
import ru.putevod.app.planner.dto.EventDto;
import ru.putevod.app.planner.dto.EventReminderDto;
import ru.putevod.app.planner.exception.BadRequestException;
import ru.putevod.app.planner.exception.ResourceNotFoundException;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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
    private EventReminder eventReminder;
    private EventReminderDto eventReminderDto;

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
        event.setTitle("Test Event");

        eventDto = new EventDto();
        eventDto.setId(1L);

        createEventDto = new CreateEventDto();
        createEventDto.setOrderPosition(1);

        eventReminder = new EventReminder();
        eventReminder.setReminderId(1L);
        eventReminder.setEvent(event);
        eventReminder.setUser(user);
        eventReminder.setSent(false);

        eventReminderDto = new EventReminderDto();
        eventReminderDto.setId(1L);

        when(userService.getUserEntityById(anyLong())).thenReturn(user);
        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(event));
        when(tripService.hasAccessToTrip(any(), any(), any())).thenReturn(true);
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
    @DisplayName("Should create event reminder with minutes before")
    void addEventReminder_WithMinutesBefore_Success() {
        eventReminderDto.setMinutesBefore(30);
        eventReminderDto.setRemindAt(null);

        when(eventReminderMapper.fromDto(any(), any(), any())).thenReturn(eventReminder);
        when(eventReminderRepository.save(any())).thenReturn(eventReminder);
        when(eventReminderMapper.toDto(any())).thenReturn(eventReminderDto);

        EventReminderDto result = eventService.addEventReminder(1L, 1L, eventReminderDto);

        assertNotNull(result);
        verify(eventReminderRepository).save(any());
        verify(eventReminderMapper).fromDto(any(), any(), any());
    }

    @Test
    @DisplayName("Should create event reminder with remind at time")
    void addEventReminder_WithRemindAt_Success() {
        eventReminderDto.setMinutesBefore(null);
        eventReminderDto.setRemindAt(LocalDateTime.now().plusHours(1));

        when(eventReminderMapper.fromDto(any(), any(), any())).thenReturn(eventReminder);
        when(eventReminderRepository.save(any())).thenReturn(eventReminder);
        when(eventReminderMapper.toDto(any())).thenReturn(eventReminderDto);

        EventReminderDto result = eventService.addEventReminder(1L, 1L, eventReminderDto);

        assertNotNull(result);
        verify(eventReminderRepository).save(any());
        verify(eventReminderMapper).fromDto(any(), any(), any());
    }

    @Test
    @DisplayName("Should throw exception when creating reminder for event without specific time")
    void addEventReminder_EventWithoutSpecificTime_ThrowsException() {
        event.setHasSpecificTime(false);
        eventReminderDto.setMinutesBefore(30);
        eventReminderDto.setRemindAt(null);

        when(eventReminderMapper.fromDto(any(), any(), any())).thenReturn(eventReminder);

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                eventService.addEventReminder(1L, 1L, eventReminderDto)
        );
        assertEquals("Невозможно создать напоминание: событие не имеет конкретного времени", exception.getMessage());
    }

    @Test
    @DisplayName("Should get event reminders for user")
    void getEventReminders_Success() {
        when(eventReminderRepository.findByEventAndUser(any(), any())).thenReturn(List.of(eventReminder));
        when(eventReminderMapper.toDto(any())).thenReturn(eventReminderDto);

        List<EventReminderDto> result = eventService.getEventReminders(1L, 1L);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(eventReminderRepository).findByEventAndUser(event, user);
    }

    @Test
    @DisplayName("Should delete event reminder")
    void deleteEventReminder_Success() {
        when(eventReminderRepository.findById(anyLong())).thenReturn(Optional.of(eventReminder));

        eventService.deleteEventReminder(1L, 1L, 1L);

        verify(eventReminderRepository).delete(eventReminder);
    }

    @Test
    @DisplayName("Should throw exception when deleting reminder with wrong user")
    void deleteEventReminder_WrongUser_ThrowsException() {
        User wrongUser = new User();
        wrongUser.setUserId(2L);
        eventReminder.setUser(wrongUser);

        when(eventReminderRepository.findById(anyLong())).thenReturn(Optional.of(eventReminder));

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                eventService.deleteEventReminder(1L, 1L, 1L)
        );
        assertEquals("Напоминание не принадлежит указанному пользователю или событию", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when deleting reminder with wrong event")
    void deleteEventReminder_WrongEvent_ThrowsException() {
        Event wrongEvent = new Event();
        wrongEvent.setEventId(2L);
        eventReminder.setEvent(wrongEvent);

        when(eventReminderRepository.findById(anyLong())).thenReturn(Optional.of(eventReminder));

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                eventService.deleteEventReminder(1L, 1L, 1L)
        );
        assertEquals("Напоминание не принадлежит указанному пользователю или событию", exception.getMessage());
    }

    @Test
    @DisplayName("Should process reminders and send notifications")
    void processReminders_Success() {
        List<EventReminder> reminders = List.of(eventReminder);
        when(eventReminderRepository.findUpcomingReminders(any(), any())).thenReturn(reminders);

        eventService.processReminders();

        verify(notificationService).createEventReminderNotification(
                eq(user.getUserId()),
                eq(event.getEventId()),
                eq(event.getTitle())
        );
        verify(eventReminderRepository).save(any());
        assertTrue(eventReminder.isSent());
    }

    @Test
    @DisplayName("Should handle notification error during reminder processing")
    void processReminders_NotificationError_ContinuesProcessing() {
        List<EventReminder> reminders = List.of(eventReminder);
        when(eventReminderRepository.findUpcomingReminders(any(), any())).thenReturn(reminders);
        doThrow(new RuntimeException("Notification error"))
                .when(notificationService)
                .createEventReminderNotification(anyLong(), anyLong(), anyString());

        eventService.processReminders();

        verify(notificationService).createEventReminderNotification(
                eq(user.getUserId()),
                eq(event.getEventId()),
                eq(event.getTitle())
        );
        verify(eventReminderRepository, never()).save(any());
        assertFalse(eventReminder.isSent());
    }

    @Test
    @DisplayName("Should throw exception when event not found")
    void getEventReminders_EventNotFound_ThrowsException() {
        when(eventRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                eventService.getEventReminders(1L, 1L)
        );
        assertEquals("Событие not found with id: '1'", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when reminder not found")
    void deleteEventReminder_ReminderNotFound_ThrowsException() {
        when(eventReminderRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                eventService.deleteEventReminder(1L, 1L, 1L)
        );
        assertEquals("Напоминание not found with id: '1'", exception.getMessage());
    }
} 