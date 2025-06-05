package ru.putevod.app.planner.service;

import ru.putevod.app.planner.dto.*;
import ru.putevod.app.planner.model.Event;

import java.util.List;

public interface EventService {

    EventDto createEvent(Long userId, Long tripId, Long dayId, CreateEventDto createEventDto);

    EventDto updateEvent(Long userId, Long tripId, Long dayId, Long eventId, UpdateEventDto updateEventDto);

    EventDto getEvent(Long userId, Long tripId, Long dayId, Long eventId);

    List<EventDto> getDayEvents(Long userId, Long tripId, Long dayId);

    List<EventDto> getTripEvents(Long userId, Long tripId);

    void deleteEvent(Long userId, Long tripId, Long dayId, Long eventId);

    List<EventDto> getUpcomingEvents(Long userId, int days);

    EventReminderDto addEventReminder(Long userId, Long eventId, CreateEventReminderDto reminderDto);

    List<EventReminderDto> getEventReminders(Long userId, Long eventId);

    void deleteEventReminder(Long userId, Long eventId, Long reminderId);

    void processReminders();

    Event getEventEntityById(Long eventId);

    /**
     * Перемещение события без времени на новую позицию
     *
     * @param userId      ID пользователя
     * @param tripId      ID поездки
     * @param dayId       ID дня
     * @param eventId     ID перемещаемого события
     * @param newPosition новая позиция (может быть между событиями со временем)
     * @return обновленное событие
     */
    EventDto reorderEvent(Long userId, Long tripId, Long dayId, Long eventId, Integer newPosition);
} 