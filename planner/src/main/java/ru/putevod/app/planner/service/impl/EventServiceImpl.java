package ru.putevod.app.planner.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.putevod.app.planner.dto.CreateEventDto;
import ru.putevod.app.planner.dto.EventDto;
import ru.putevod.app.planner.dto.EventReminderDto;
import ru.putevod.app.planner.dto.PlaceDto;
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
import ru.putevod.app.planner.service.EventService;
import ru.putevod.app.planner.service.NotificationService;
import ru.putevod.app.planner.service.TripService;
import ru.putevod.app.planner.service.UserService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final TripDayRepository tripDayRepository;
    private final EventReminderRepository eventReminderRepository;
    private final PlaceRepository placeRepository;
    private final TripService tripService;
    private final UserService userService;
    private final NotificationService notificationService;
    private final EventMapper eventMapper;
    private final CreateEventMapper createEventMapper;
    private final PlaceMapper placeMapper;
    private final EventReminderMapper eventReminderMapper;

    @Override
    @Transactional
    public EventDto createEvent(Long userId, Long tripId, Long dayId, CreateEventDto createEventDto) {
        Trip trip = tripService.getTripEntityWithAccessCheck(userId, tripId);

        User user = userService.getUserEntityById(userId);
        if (!tripService.hasAccessToTrip(user, trip, "admin", "write")) {
            throw new BadRequestException("У вас нет прав на создание событий в этой поездке");
        }

        TripDay day = tripDayRepository.findByTripAndDayId(trip, dayId)
                .orElseThrow(() -> new ResourceNotFoundException("День", "id", dayId));

        if (createEventDto.getOrderPosition() == null) {
            Integer lastPosition = 0;
            List<Event> existingEvents = eventRepository.findByDayOrderByOrderPositionAsc(day);
            if (!existingEvents.isEmpty()) {
                Event lastEvent = existingEvents.getLast();
                lastPosition = lastEvent.getOrderPosition();
            }
            createEventDto.setOrderPosition(lastPosition + 1);
        }

        List<Event> events = eventRepository.findByDayOrderByOrderPositionAsc(day);

        events.stream()
                .filter(e -> e.getOrderPosition() >= createEventDto.getOrderPosition())
                .forEach(e -> e.setOrderPosition(e.getOrderPosition() + 1));

        Event event = createEventMapper.fromDto(createEventDto, day);

        if (createEventDto.getPlace() != null) {
            PlaceDto placeDto = createEventMapper.toPlaceDto(createEventDto.getPlace());

            Place place = placeMapper.toEntity(placeDto);
            place = placeRepository.save(place);

            event.setPlace(place);
        }

        event = eventRepository.save(event);

        return eventMapper.toDto(event);
    }

    @Override
    @Transactional
    public EventDto updateEvent(Long userId, Long tripId, Long dayId, Long eventId, EventDto eventDto) {
        Trip trip = tripService.getTripEntityWithAccessCheck(userId, tripId);

        // Проверяем доступ на запись
        User user = userService.getUserEntityById(userId);
        if (!tripService.hasAccessToTrip(user, trip, "admin", "write")) {
            throw new BadRequestException("У вас нет прав на редактирование событий в этой поездке");
        }

        TripDay day = tripDayRepository.findByTripAndDayId(trip, dayId)
                .orElseThrow(() -> new ResourceNotFoundException("День", "id", dayId));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Событие", "id", eventId));

        if (!event.getDay().getDayId().equals(dayId)) {
            throw new BadRequestException("Событие не принадлежит указанному дню");
        }

        eventMapper.updateEntityFromDto(eventDto, event);
        event = eventRepository.save(event);

        return eventMapper.toDto(event);
    }

    @Override
    @Transactional(readOnly = true)
    public EventDto getEvent(Long userId, Long tripId, Long dayId, Long eventId) {
        Trip trip = tripService.getTripEntityWithAccessCheck(userId, tripId);

        // Проверяем доступ на чтение
        User user = userService.getUserEntityById(userId);
        if (!tripService.hasAccessToTrip(user, trip, "admin", "read", "write")) {
            throw new BadRequestException("У вас нет прав на просмотр событий в этой поездке");
        }

        TripDay day = tripDayRepository.findByTripAndDayId(trip, dayId)
                .orElseThrow(() -> new ResourceNotFoundException("День", "id", dayId));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Событие", "id", eventId));

        if (!event.getDay().getDayId().equals(dayId)) {
            throw new BadRequestException("Событие не принадлежит указанному дню");
        }

        return eventMapper.toDto(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventDto> getDayEvents(Long userId, Long tripId, Long dayId) {
        Trip trip = tripService.getTripEntityWithAccessCheck(userId, tripId);

        User user = userService.getUserEntityById(userId);
        if (!tripService.hasAccessToTrip(user, trip, "admin", "read", "write")) {
            throw new BadRequestException("У вас нет прав на просмотр событий в этой поездке");
        }

        TripDay day = tripDayRepository.findByTripAndDayId(trip, dayId)
                .orElseThrow(() -> new ResourceNotFoundException("День", "id", dayId));

        List<Event> events = eventRepository.findByDayOrderByOrderPositionAsc(day);

        return events.stream()
                .map(eventMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventDto> getTripEvents(Long userId, Long tripId) {
        Trip trip = tripService.getTripEntityWithAccessCheck(userId, tripId);

        User user = userService.getUserEntityById(userId);
        if (!tripService.hasAccessToTrip(user, trip, "admin", "read", "write")) {
            throw new BadRequestException("У вас нет прав на просмотр событий в этой поездке");
        }

        List<Event> events = eventRepository.findAllByTrip(trip);

        return events.stream()
                .map(eventMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteEvent(Long userId, Long tripId, Long dayId, Long eventId) {
        Trip trip = tripService.getTripEntityWithAccessCheck(userId, tripId);

        User user = userService.getUserEntityById(userId);
        if (!tripService.hasAccessToTrip(user, trip, "admin", "write")) {
            throw new BadRequestException("У вас нет прав на удаление событий в этой поездке");
        }

        TripDay day = tripDayRepository.findByTripAndDayId(trip, dayId)
                .orElseThrow(() -> new ResourceNotFoundException("День", "id", dayId));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Событие", "id", eventId));

        if (!event.getDay().getDayId().equals(dayId)) {
            throw new BadRequestException("Событие не принадлежит указанному дню");
        }

        eventRepository.delete(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventDto> getUpcomingEvents(Long userId, int days) {
        User user = userService.getUserEntityById(userId);

        List<Event> events = eventRepository.findUpcomingEventsForUser(user, days);

        return events.stream()
                .map(eventMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventReminderDto addEventReminder(Long userId, Long eventId, EventReminderDto reminderDto) {
        User user = userService.getUserEntityById(userId);
        Event event = getEventEntityById(eventId);

        Trip trip = event.getDay().getTrip();
        tripService.hasAccessToTrip(user, trip, "admin", "read", "write");

        EventReminder reminder = eventReminderMapper.fromDto(reminderDto, event, user);

        if (reminderDto.getRemindAt() == null && reminderDto.getMinutesBefore() != null) {
            if (event.isHasSpecificTime() && event.getStartTime() != null) {
                LocalDateTime eventDateTime = event.getDay().getDate().atTime(event.getStartTime());
                LocalDateTime remindAt = eventDateTime.minusMinutes(reminderDto.getMinutesBefore());
                reminder.setRemindAt(remindAt);
            } else {
                throw new BadRequestException("Невозможно создать напоминание: событие не имеет конкретного времени");
            }
        } else if (reminderDto.getRemindAt() != null && reminderDto.getMinutesBefore() == null) {
            if (event.isHasSpecificTime() && event.getStartTime() != null) {
                LocalDateTime eventDateTime = event.getDay().getDate().atTime(event.getStartTime());
                long minutes = ChronoUnit.MINUTES.between(reminderDto.getRemindAt(), eventDateTime);
                reminder.setMinutesBefore((int) minutes);
            } else {
                reminder.setMinutesBefore(0);
            }
        }

        reminder.setSent(false);
        reminder = eventReminderRepository.save(reminder);

        return eventReminderMapper.toDto(reminder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventReminderDto> getEventReminders(Long userId, Long eventId) {
        User user = userService.getUserEntityById(userId);
        Event event = getEventEntityById(eventId);

        Trip trip = event.getDay().getTrip();
        tripService.hasAccessToTrip(user, trip, "admin", "read", "write");

        List<EventReminder> reminders = eventReminderRepository.findByEventAndUser(event, user);

        return reminders.stream()
                .map(eventReminderMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteEventReminder(Long userId, Long eventId, Long reminderId) {
        User user = userService.getUserEntityById(userId);
        Event event = getEventEntityById(eventId);

        Trip trip = event.getDay().getTrip();
        tripService.hasAccessToTrip(user, trip, "admin", "read", "write");

        EventReminder reminder = eventReminderRepository.findById(reminderId)
                .orElseThrow(() -> new ResourceNotFoundException("Напоминание", "id", reminderId));

        if (!reminder.getUser().getUserId().equals(userId) || !reminder.getEvent().getEventId().equals(eventId)) {
            throw new BadRequestException("Напоминание не принадлежит указанному пользователю или событию");
        }

        eventReminderRepository.delete(reminder);
    }

    @Override
    @Transactional
    public void processReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourLater = now.plusHours(1);

        List<EventReminder> reminders = eventReminderRepository.findUpcomingReminders(now, oneHourLater);

        for (EventReminder reminder : reminders) {
            if (!reminder.isSent()) {
                try {
                    String content = String.format(
                            "Напоминание о событии \"%s\" в %s",
                            reminder.getEvent().getTitle(),
                            reminder.getEvent().getStartTime() != null
                                    ? reminder.getEvent().getStartTime().toString()
                                    : "течение дня"
                    );

                    notificationService.createEventReminderNotification(
                            reminder.getUser().getUserId(),
                            reminder.getEvent().getEventId(),
                            reminder.getEvent().getTitle()
                    );

                    reminder.setSent(true);
                    eventReminderRepository.save(reminder);

                    log.info("Отправлено напоминание [{}] для пользователя [{}]", reminder.getReminderId(), reminder.getUser().getUserId());
                } catch (Exception e) {
                    log.error("Ошибка при отправке напоминания [{}]: {}", reminder.getReminderId(), e.getMessage());
                }
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Event getEventEntityById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Событие", "id", eventId));
    }
} 