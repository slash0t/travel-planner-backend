package ru.putevod.app.planner.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.putevod.app.planner.dto.*;
import ru.putevod.app.planner.exception.BadRequestException;
import ru.putevod.app.planner.exception.ResourceNotFoundException;
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
import java.time.LocalTime;
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

        // Сквозная нумерация: определяем позицию для нового события
        if (createEventDto.getOrderPosition() == null) {
            if (Boolean.TRUE.equals(createEventDto.getHasSpecificTime()) && createEventDto.getStartTime() != null) {
                // Для события со временем находим правильную позицию среди всех событий
                createEventDto.setOrderPosition(calculatePositionForTimedEventInSequence(day, createEventDto.getStartTime()));
            } else {
                // Для события без времени добавляем в начало (позиция 1)
                createEventDto.setOrderPosition(1);
                shiftAllEventsPosition(day, 1, 1);
            }
        } else {
            // Если позиция указана явно - вставляем туда, сдвигая остальные
            shiftAllEventsPosition(day, createEventDto.getOrderPosition(), 1);
        }

        Event event = eventMapper.toEntityFromCreate(createEventDto, day);

        if (createEventDto.getPlace() != null) {
            Place place = new Place();
            place.setName(createEventDto.getPlace().getName());
            place.setLatitude(createEventDto.getPlace().getLatitude());
            place.setLongitude(createEventDto.getPlace().getLongitude());
            place.setAddress(createEventDto.getPlace().getAddress());
            place.setPlaceType(createEventDto.getPlace().getPlaceType());
            place.setExternalId(createEventDto.getPlace().getExternalId());
            place.setPreviewUrl(createEventDto.getPlace().getPreviewUrl());

            place = placeRepository.save(place);
            event.setPlace(place);
        }

        event = eventRepository.save(event);

        return eventMapper.toDto(event);
    }

    /**
     * Вычисляет правильную позицию для события со временем в общей последовательности
     */
    private Integer calculatePositionForTimedEventInSequence(TripDay day, LocalTime startTime) {
        // Получаем все события дня, отсортированные по позиции
        List<Event> allEvents = eventRepository.findByDayOrderByTimeAndPosition(day);

        int insertPosition = allEvents.size() + 1; // По умолчанию в конец

        for (int i = 0; i < allEvents.size(); i++) {
            Event currentEvent = allEvents.get(i);

            // Если текущее событие имеет время и оно позже нашего - вставляем перед ним
            if (currentEvent.isHasSpecificTime() && currentEvent.getStartTime() != null) {
                if (currentEvent.getStartTime().isAfter(startTime)) {
                    insertPosition = currentEvent.getOrderPosition();
                    break;
                }
            }
        }

        // Сдвигаем все события с позиции insertPosition на 1 вправо
        shiftAllEventsPosition(day, insertPosition, 1);

        return insertPosition;
    }

    /**
     * Сдвигает позиции ВСЕХ событий (и с временем, и без) начиная с указанной позиции
     */
    private void shiftAllEventsPosition(TripDay day, int fromPosition, int shift) {
        List<Event> allEvents = eventRepository.findByDayOrderByTimeAndPosition(day);

        allEvents.stream()
                .filter(e -> e.getOrderPosition() >= fromPosition)
                .forEach(e -> e.setOrderPosition(e.getOrderPosition() + shift));

        if (!allEvents.isEmpty()) {
            eventRepository.saveAll(allEvents);
        }
    }

    @Override
    @Transactional
    public EventDto updateEvent(Long userId, Long tripId, Long dayId, Long eventId, UpdateEventDto updateEventDto) {
        Trip trip = tripService.getTripEntityWithAccessCheck(userId, tripId);

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

        boolean wasTimedEvent = event.isHasSpecificTime() && event.getStartTime() != null;
        LocalTime oldStartTime = event.getStartTime();
        int oldPosition = event.getOrderPosition();

        eventMapper.updateEntityFromUpdate(updateEventDto, event);

        // Обработка обновления места
        if (updateEventDto.getPlace() != null) {
            // Если передано место - обновляем или создаем новое
            Place place;
            if (event.getPlace() != null) {
                // Обновляем существующее место
                place = event.getPlace();
                place.setName(updateEventDto.getPlace().getName());
                place.setLatitude(updateEventDto.getPlace().getLatitude());
                place.setLongitude(updateEventDto.getPlace().getLongitude());
                place.setAddress(updateEventDto.getPlace().getAddress());
                place.setPlaceType(updateEventDto.getPlace().getPlaceType());
                place.setExternalId(updateEventDto.getPlace().getExternalId());
                place.setPreviewUrl(updateEventDto.getPlace().getPreviewUrl());
            } else {
                // Создаем новое место
                place = new Place();
                place.setName(updateEventDto.getPlace().getName());
                place.setLatitude(updateEventDto.getPlace().getLatitude());
                place.setLongitude(updateEventDto.getPlace().getLongitude());
                place.setAddress(updateEventDto.getPlace().getAddress());
                place.setPlaceType(updateEventDto.getPlace().getPlaceType());
                place.setExternalId(updateEventDto.getPlace().getExternalId());
                place.setPreviewUrl(updateEventDto.getPlace().getPreviewUrl());
            }
            place = placeRepository.save(place);
            event.setPlace(place);
        } else if (updateEventDto.getPlace() == null && event.getPlace() != null) {
            // Если место явно передано как null и у события было место - удаляем связь
            // Примечание: само место не удаляем, так как оно может использоваться в других событиях
            event.setPlace(null);
        }

        boolean willBeTimedEvent = event.isHasSpecificTime() && event.getStartTime() != null;

        if (wasTimedEvent != willBeTimedEvent) {
            if (willBeTimedEvent) {
                shiftAllEventsPositionAfterDeletion(day, oldPosition);
                event.setOrderPosition(calculatePositionForTimedEventInSequence(day, event.getStartTime()));
            } else {
                shiftAllEventsPositionAfterDeletion(day, oldPosition);
                shiftAllEventsPosition(day, 1, 1);
                event.setOrderPosition(1);
            }
        } else if (willBeTimedEvent && !event.getStartTime().equals(oldStartTime)) {
            shiftAllEventsPositionAfterDeletion(day, oldPosition);
            event.setOrderPosition(calculatePositionForTimedEventInSequence(day, event.getStartTime()));
        }

        event = eventRepository.save(event);

        return eventMapper.toDto(event);
    }

    @Override
    @Transactional(readOnly = true)
    public EventDto getEvent(Long userId, Long tripId, Long dayId, Long eventId) {
        Trip trip = tripService.getTripEntityWithAccessCheck(userId, tripId);

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

        TripDay tripDay = tripDayRepository.findByTripAndDayId(trip, dayId)
                .orElseThrow(() -> new ResourceNotFoundException("День", "id", dayId));

        List<Event> allEvents = eventRepository.findByDayOrderByTimeAndPosition(tripDay);

        return allEvents.stream()
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

        int deletedEventPosition = event.getOrderPosition();

        eventRepository.delete(event);

        shiftAllEventsPositionAfterDeletion(day, deletedEventPosition);
    }

    /**
     * Сдвигает позиции всех событий после удаления события
     */
    private void shiftAllEventsPositionAfterDeletion(TripDay day, int deletedPosition) {
        List<Event> allEvents = eventRepository.findByDayOrderByTimeAndPosition(day);

        allEvents.stream()
                .filter(e -> e.getOrderPosition() > deletedPosition)
                .forEach(e -> e.setOrderPosition(e.getOrderPosition() - 1));

        if (!allEvents.isEmpty()) {
            eventRepository.saveAll(allEvents);
        }
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
    public EventReminderDto addEventReminder(Long userId, Long eventId, CreateEventReminderDto reminderDto) {
        User user = userService.getUserEntityById(userId);
        Event event = getEventEntityById(eventId);

        Trip trip = event.getDay().getTrip();
        tripService.hasAccessToTrip(user, trip, "admin", "read", "write");

        EventReminder reminder = eventReminderMapper.toEntityFromCreate(reminderDto, event, user);

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

    @Override
    @Transactional
    public EventDto reorderEvent(Long userId, Long tripId, Long dayId, Long eventId, Integer newPosition) {
        Trip trip = tripService.getTripEntityWithAccessCheck(userId, tripId);

        User user = userService.getUserEntityById(userId);
        if (!tripService.hasAccessToTrip(user, trip, "admin", "write")) {
            throw new BadRequestException("У вас нет прав на перемещение событий в этой поездке");
        }

        TripDay day = tripDayRepository.findByTripAndDayId(trip, dayId)
                .orElseThrow(() -> new ResourceNotFoundException("День", "id", dayId));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Событие", "id", eventId));

        if (!event.getDay().getDayId().equals(dayId)) {
            throw new BadRequestException("Событие не принадлежит указанному дню");
        }

        if (event.isHasSpecificTime() && event.getStartTime() != null) {
            throw new BadRequestException("Нельзя перемещать события с конкретным временем. Они автоматически позиционируются по времени.");
        }

        List<Event> allEvents = eventRepository.findByDayOrderByTimeAndPosition(day);

        int maxPosition = allEvents.size();

        if (newPosition < 1 || newPosition > maxPosition) {
            throw new BadRequestException("Позиция должна быть от 1 до " + maxPosition);
        }

        int currentPosition = event.getOrderPosition();

        if (currentPosition == newPosition) {
            return eventMapper.toDto(event);
        }

        if (currentPosition < newPosition) {
            allEvents.stream()
                    .filter(e -> !e.getEventId().equals(eventId))
                    .filter(e -> e.getOrderPosition() > currentPosition && e.getOrderPosition() <= newPosition)
                    .forEach(e -> e.setOrderPosition(e.getOrderPosition() - 1));
        } else {
            allEvents.stream()
                    .filter(e -> !e.getEventId().equals(eventId))
                    .filter(e -> e.getOrderPosition() >= newPosition && e.getOrderPosition() < currentPosition)
                    .forEach(e -> e.setOrderPosition(e.getOrderPosition() + 1));
        }

        event.setOrderPosition(newPosition);
        eventRepository.saveAll(allEvents);
        event = eventRepository.save(event);

        return eventMapper.toDto(event);
    }
} 