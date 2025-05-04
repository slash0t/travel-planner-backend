package ru.putevod.app.planner.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.putevod.app.planner.dto.EventDto;
import ru.putevod.app.planner.dto.EventReminderDto;
import ru.putevod.app.planner.exception.BadRequestException;
import ru.putevod.app.planner.exception.ResourceNotFoundException;
import ru.putevod.app.planner.mapper.EventMapper;
import ru.putevod.app.planner.mapper.EventReminderMapper;
import ru.putevod.app.planner.model.Event;
import ru.putevod.app.planner.model.EventReminder;
import ru.putevod.app.planner.model.Trip;
import ru.putevod.app.planner.model.TripDay;
import ru.putevod.app.planner.model.User;
import ru.putevod.app.planner.repository.EventReminderRepository;
import ru.putevod.app.planner.repository.EventRepository;
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
    private final TripService tripService;
    private final UserService userService;
    private final NotificationService notificationService;
    private final EventMapper eventMapper;
    private final EventReminderMapper eventReminderMapper;

    @Override
    @Transactional
    public EventDto createEvent(Long userId, Long tripId, Long dayId, EventDto eventDto) {
        Trip trip = tripService.getTripEntityWithAccessCheck(userId, tripId);
        
        // Проверяем доступ на запись
        User user = userService.getUserEntityById(userId);
        if (!tripService.hasAccessToTrip(user, trip, "admin", "write")) {
            throw new BadRequestException("У вас нет прав на создание событий в этой поездке");
        }
        
        TripDay day = tripDayRepository.findByTripAndDayId(trip, dayId)
                .orElseThrow(() -> new ResourceNotFoundException("День", "id", dayId));
        
        if (eventDto.getOrderPosition() == null) {
            // Если не указан порядковый номер, ставим в конец списка
            Integer lastPosition = 0;
            List<Event> existingEvents = eventRepository.findByDayOrderByOrderPositionAsc(day);
            if (!existingEvents.isEmpty()) {
                Event lastEvent = existingEvents.get(existingEvents.size() - 1);
                lastPosition = lastEvent.getOrderPosition();
            }
            eventDto.setOrderPosition(lastPosition + 1);
        }
        
        // Сортируем элементы по позиции
        List<Event> events = eventRepository.findByDayOrderByOrderPositionAsc(day);
        
        // Если новая позиция в середине списка, сдвигаем остальные элементы
        events.stream()
                .filter(e -> e.getOrderPosition() >= eventDto.getOrderPosition())
                .forEach(e -> e.setOrderPosition(e.getOrderPosition() + 1));
        
        Event event = eventMapper.fromDto(eventDto, day);
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
        
        // Проверяем, что событие принадлежит указанному дню
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
        
        // Проверяем, что событие принадлежит указанному дню
        if (!event.getDay().getDayId().equals(dayId)) {
            throw new BadRequestException("Событие не принадлежит указанному дню");
        }
        
        return eventMapper.toDto(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventDto> getDayEvents(Long userId, Long tripId, Long dayId) {
        Trip trip = tripService.getTripEntityWithAccessCheck(userId, tripId);
        
        // Проверяем доступ на чтение
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
        
        // Проверяем доступ на чтение
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
        
        // Проверяем доступ на запись
        User user = userService.getUserEntityById(userId);
        if (!tripService.hasAccessToTrip(user, trip, "admin", "write")) {
            throw new BadRequestException("У вас нет прав на удаление событий в этой поездке");
        }
        
        TripDay day = tripDayRepository.findByTripAndDayId(trip, dayId)
                .orElseThrow(() -> new ResourceNotFoundException("День", "id", dayId));
        
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Событие", "id", eventId));
        
        // Проверяем, что событие принадлежит указанному дню
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
        
        // Проверяем, что пользователь имеет доступ к поездке, в которой находится событие
        Trip trip = event.getDay().getTrip();
        tripService.hasAccessToTrip(user, trip, "admin", "read", "write");
        
        // Создаем напоминание
        EventReminder reminder = eventReminderMapper.fromDto(reminderDto, event, user);
        
        // Если не указано точное время напоминания, но указано кол-во минут до события,
        // вычисляем время напоминания автоматически
        if (reminderDto.getRemindAt() == null && reminderDto.getMinutesBefore() != null) {
            if (event.isHasSpecificTime() && event.getStartTime() != null) {
                LocalDateTime eventDateTime = event.getDay().getDate().atTime(event.getStartTime());
                LocalDateTime remindAt = eventDateTime.minusMinutes(reminderDto.getMinutesBefore());
                reminder.setRemindAt(remindAt);
            } else {
                throw new BadRequestException("Невозможно создать напоминание: событие не имеет конкретного времени");
            }
        } else if (reminderDto.getRemindAt() != null && reminderDto.getMinutesBefore() == null) {
            // Если указано точное время напоминания, но не указано кол-во минут до события,
            // вычисляем кол-во минут автоматически
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
        
        // Проверяем, что пользователь имеет доступ к поездке, в которой находится событие
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
        
        // Проверяем, что пользователь имеет доступ к поездке, в которой находится событие
        Trip trip = event.getDay().getTrip();
        tripService.hasAccessToTrip(user, trip, "admin", "write");
        
        EventReminder reminder = eventReminderRepository.findById(reminderId)
                .orElseThrow(() -> new ResourceNotFoundException("Напоминание", "id", reminderId));
        
        // Проверяем, что напоминание принадлежит указанному событию и пользователю
        if (!reminder.getEvent().getEventId().equals(eventId)) {
            throw new BadRequestException("Напоминание не принадлежит указанному событию");
        }
        
        if (!reminder.getUser().getUserId().equals(userId)) {
            throw new BadRequestException("У вас нет прав на удаление этого напоминания");
        }
        
        eventReminderRepository.delete(reminder);
    }

    @Override
    @Transactional
    public void processReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = now.plusMinutes(10); // Проверяем напоминания на ближайшие 10 минут
        
        log.debug("Checking reminders between {} and {}", now, endTime);
        
        List<EventReminder> dueReminders = eventReminderRepository.findDueReminders(now, endTime);
        
        if (!dueReminders.isEmpty()) {
            log.debug("Found {} due reminders", dueReminders.size());
            
            // Группируем напоминания по пользователям и событиям для отправки уведомлений
            for (EventReminder reminder : dueReminders) {
                try {
                    // Отправляем уведомление пользователю
                    notificationService.createEventReminderNotification(
                            reminder.getUser().getUserId(),
                            reminder.getEvent().getEventId(),
                            reminder.getEvent().getTitle());
                } catch (Exception e) {
                    log.error("Error sending notification for reminder {}", reminder.getReminderId(), e);
                }
            }
            
            // Отмечаем напоминания как отправленные
            List<Long> reminderIds = dueReminders.stream()
                    .map(EventReminder::getReminderId)
                    .collect(Collectors.toList());
            
            eventReminderRepository.markAsSent(reminderIds);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Event getEventEntityById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Событие", "id", eventId));
    }
} 