package ru.putevod.app.planner.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.putevod.app.planner.dto.CreateEventDto;
import ru.putevod.app.planner.dto.EventDto;
import ru.putevod.app.planner.exception.BadRequestException;
import ru.putevod.app.planner.mapper.EventMapper;
import ru.putevod.app.planner.mapper.PlaceMapper;
import ru.putevod.app.planner.mapper.EventReminderMapper;
import ru.putevod.app.planner.model.*;
import ru.putevod.app.planner.repository.*;
import ru.putevod.app.planner.service.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceImplReorderTest {

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
    private PlaceMapper placeMapper;
    @Mock
    private EventReminderMapper eventReminderMapper;

    @InjectMocks
    private EventServiceImpl eventService;

    private User user;
    private Trip trip;
    private TripDay tripDay;
    private Event untimedEvent1, untimedEvent2, untimedEvent3;
    private Event timedEvent1, timedEvent2;

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

        // События без времени
        untimedEvent1 = Event.builder()
                .eventId(1L)
                .day(tripDay)
                .title("Untimed Event 1")
                .hasSpecificTime(false)
                .orderPosition(1)
                .build();

        untimedEvent2 = Event.builder()
                .eventId(2L)
                .day(tripDay)
                .title("Untimed Event 2")
                .hasSpecificTime(false)
                .orderPosition(2)
                .build();

        untimedEvent3 = Event.builder()
                .eventId(3L)
                .day(tripDay)
                .title("Untimed Event 3")
                .hasSpecificTime(false)
                .orderPosition(3)
                .build();

        // События со временем
        timedEvent1 = Event.builder()
                .eventId(4L)
                .day(tripDay)
                .title("Timed Event 1")
                .hasSpecificTime(true)
                .startTime(LocalTime.of(10, 0))
                .orderPosition(4)
                .build();

        timedEvent2 = Event.builder()
                .eventId(5L)
                .day(tripDay)
                .title("Timed Event 2")
                .hasSpecificTime(true)
                .startTime(LocalTime.of(14, 0))
                .orderPosition(5)
                .build();
    }

    @Test
    void reorderEvent_Success_MoveUntimedEventDown() {
        when(tripService.getTripEntityWithAccessCheck(anyLong(), anyLong())).thenReturn(trip);
        when(userService.getUserEntityById(anyLong())).thenReturn(user);
        when(tripService.hasAccessToTrip(any(User.class), any(Trip.class), eq("admin"), eq("write"))).thenReturn(true);
        when(tripDayRepository.findByTripAndDayId(any(), any())).thenReturn(Optional.of(tripDay));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(untimedEvent1));
        when(eventRepository.findUntimedEventsByDay(tripDay)).thenReturn(Arrays.asList(untimedEvent1, untimedEvent2, untimedEvent3));
        when(eventRepository.save(any())).thenReturn(untimedEvent1);
        when(eventMapper.toDto(any())).thenReturn(new EventDto());

        EventDto result = eventService.reorderEvent(1L, 1L, 1L, 1L, 3);

        assertNotNull(result);
        assertEquals(3, untimedEvent1.getOrderPosition());
        assertEquals(1, untimedEvent2.getOrderPosition());
        assertEquals(2, untimedEvent3.getOrderPosition());
        verify(eventRepository).saveAll(any());
    }

    @Test
    void reorderEvent_ThrowsException_WhenTryingToMoveTimedEvent() {
        when(tripService.getTripEntityWithAccessCheck(anyLong(), anyLong())).thenReturn(trip);
        when(userService.getUserEntityById(anyLong())).thenReturn(user);
        when(tripService.hasAccessToTrip(any(User.class), any(Trip.class), eq("admin"), eq("write"))).thenReturn(true);
        when(tripDayRepository.findByTripAndDayId(any(), any())).thenReturn(Optional.of(tripDay));
        when(eventRepository.findById(4L)).thenReturn(Optional.of(timedEvent1));

        BadRequestException exception = assertThrows(BadRequestException.class, 
                () -> eventService.reorderEvent(1L, 1L, 1L, 4L, 2));
        
        assertTrue(exception.getMessage().contains("Нельзя перемещать события с конкретным временем"));
    }

    @Test
    void createEvent_UntimedEvent_PlacedAtBeginning() {
        CreateEventDto createDto = CreateEventDto.builder()
                .title("New Untimed Event")
                .hasSpecificTime(false)
                .build();

        when(tripService.getTripEntityWithAccessCheck(anyLong(), anyLong())).thenReturn(trip);
        when(userService.getUserEntityById(anyLong())).thenReturn(user);
        when(tripService.hasAccessToTrip(any(User.class), any(Trip.class), eq("admin"), eq("write"))).thenReturn(true);
        when(tripDayRepository.findByTripAndDayId(any(), any())).thenReturn(Optional.of(tripDay));
        when(eventRepository.findUntimedEventsByDay(tripDay)).thenReturn(Arrays.asList(untimedEvent1, untimedEvent2));
        when(eventMapper.toEntityFromCreate(any(), any())).thenReturn(untimedEvent1);
        when(eventRepository.save(any())).thenReturn(untimedEvent1);
        when(eventMapper.toDto(any())).thenReturn(new EventDto());

        EventDto result = eventService.createEvent(1L, 1L, 1L, createDto);

        assertNotNull(result);
        assertEquals(1, createDto.getOrderPosition());
        verify(eventRepository).saveAll(any());
    }

    @Test
    void createEvent_TimedEvent_PlacedByTime() {
        CreateEventDto createDto = CreateEventDto.builder()
                .title("New Timed Event")
                .hasSpecificTime(true)
                .startTime(LocalTime.of(12, 0)) 
                .build();

        when(tripService.getTripEntityWithAccessCheck(anyLong(), anyLong())).thenReturn(trip);
        when(userService.getUserEntityById(anyLong())).thenReturn(user);
        when(tripService.hasAccessToTrip(any(User.class), any(Trip.class), eq("admin"), eq("write"))).thenReturn(true);
        when(tripDayRepository.findByTripAndDayId(any(), any())).thenReturn(Optional.of(tripDay));
        when(eventRepository.findTimedEventsByDay(tripDay)).thenReturn(Arrays.asList(timedEvent1, timedEvent2));
        when(eventRepository.findUntimedEventsByDay(tripDay)).thenReturn(Arrays.asList(untimedEvent1, untimedEvent2));
        when(eventMapper.toEntityFromCreate(any(), any())).thenReturn(timedEvent1);
        when(eventRepository.save(any())).thenReturn(timedEvent1);
        when(eventMapper.toDto(any())).thenReturn(new EventDto());

        EventDto result = eventService.createEvent(1L, 1L, 1L, createDto);

        assertNotNull(result);
        assertEquals(4, createDto.getOrderPosition());
    }

    @Test
    void deleteEvent_UntimedEvent_ReordersRemainingEvents() {
        when(tripService.getTripEntityWithAccessCheck(anyLong(), anyLong())).thenReturn(trip);
        when(userService.getUserEntityById(anyLong())).thenReturn(user);
        when(tripService.hasAccessToTrip(any(User.class), any(Trip.class), eq("admin"), eq("write"))).thenReturn(true);
        when(tripDayRepository.findByTripAndDayId(any(), any())).thenReturn(Optional.of(tripDay));
        when(eventRepository.findById(2L)).thenReturn(Optional.of(untimedEvent2));
        when(eventRepository.findUntimedEventsByDay(tripDay)).thenReturn(Arrays.asList(untimedEvent1, untimedEvent3));

        eventService.deleteEvent(1L, 1L, 1L, 2L);

        verify(eventRepository).delete(untimedEvent2);
        assertEquals(2, untimedEvent3.getOrderPosition());
        verify(eventRepository).saveAll(any()); 
    }

    @Test
    void deleteEvent_TimedEvent_NoReordering() {
        when(tripService.getTripEntityWithAccessCheck(anyLong(), anyLong())).thenReturn(trip);
        when(userService.getUserEntityById(anyLong())).thenReturn(user);
        when(tripService.hasAccessToTrip(any(User.class), any(Trip.class), eq("admin"), eq("write"))).thenReturn(true);
        when(tripDayRepository.findByTripAndDayId(any(), any())).thenReturn(Optional.of(tripDay));
        when(eventRepository.findById(4L)).thenReturn(Optional.of(timedEvent1));

        eventService.deleteEvent(1L, 1L, 1L, 4L);

        verify(eventRepository).delete(timedEvent1);
        verify(eventRepository, never()).saveAll(any());
    }

    @Test
    void deleteEvent_FirstUntimedEvent_ReordersCorrectly() {
        when(tripService.getTripEntityWithAccessCheck(anyLong(), anyLong())).thenReturn(trip);
        when(userService.getUserEntityById(anyLong())).thenReturn(user);
        when(tripService.hasAccessToTrip(any(User.class), any(Trip.class), eq("admin"), eq("write"))).thenReturn(true);
        when(tripDayRepository.findByTripAndDayId(any(), any())).thenReturn(Optional.of(tripDay));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(untimedEvent1));
        when(eventRepository.findUntimedEventsByDay(tripDay)).thenReturn(Arrays.asList(untimedEvent2, untimedEvent3));

        eventService.deleteEvent(1L, 1L, 1L, 1L);

        verify(eventRepository).delete(untimedEvent1);
        assertEquals(1, untimedEvent2.getOrderPosition());
        assertEquals(2, untimedEvent3.getOrderPosition());
        verify(eventRepository).saveAll(any());
    }
} 