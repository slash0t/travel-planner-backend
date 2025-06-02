package ru.putevod.app.planner.service;

import ru.putevod.app.planner.dto.CreateEventDto;
import ru.putevod.app.planner.dto.EventDto;
import ru.putevod.app.planner.dto.EventReminderDto;
import ru.putevod.app.planner.model.Event;

import java.util.List;

public interface EventService {

    EventDto createEvent(Long userId, Long tripId, Long dayId, CreateEventDto createEventDto);

    EventDto updateEvent(Long userId, Long tripId, Long dayId, Long eventId, EventDto eventDto);

    EventDto getEvent(Long userId, Long tripId, Long dayId, Long eventId);

    List<EventDto> getDayEvents(Long userId, Long tripId, Long dayId);

    List<EventDto> getTripEvents(Long userId, Long tripId);

    void deleteEvent(Long userId, Long tripId, Long dayId, Long eventId);

    List<EventDto> getUpcomingEvents(Long userId, int days);

    EventReminderDto addEventReminder(Long userId, Long eventId, EventReminderDto reminderDto);

    List<EventReminderDto> getEventReminders(Long userId, Long eventId);

    void deleteEventReminder(Long userId, Long eventId, Long reminderId);

    void processReminders();

    Event getEventEntityById(Long eventId);
} 