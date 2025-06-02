package ru.putevod.app.planner.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.putevod.app.planner.config.CurrentUser;
import ru.putevod.app.planner.dto.CreateEventDto;
import ru.putevod.app.planner.dto.EventDto;
import ru.putevod.app.planner.dto.EventReminderDto;
import ru.putevod.app.planner.service.EventService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trips/{tripId}/days/{dayId}/events")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Events", description = "API для управления событиями и местами поездок")
@SecurityRequirement(name = "bearerAuth")
public class EventController {
    private final EventService eventService;

    @PostMapping
    @Operation(summary = "Создать событие")
    public ResponseEntity<EventDto> createEvent(
            @CurrentUser Long userId,
            @PathVariable Long tripId,
            @PathVariable Long dayId,
            @RequestBody CreateEventDto createEventDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(eventService.createEvent(userId, tripId, dayId, createEventDto));
    }

    @GetMapping
    @Operation(summary = "Получить все события дня")
    public ResponseEntity<List<EventDto>> getDayEvents(
            @CurrentUser Long userId,
            @PathVariable Long tripId,
            @PathVariable Long dayId) {
        return ResponseEntity.ok(eventService.getDayEvents(userId, tripId, dayId));
    }

    @GetMapping("/{eventId}")
    @Operation(summary = "Получить событие по ID")
    public ResponseEntity<EventDto> getEvent(
            @CurrentUser Long userId,
            @PathVariable Long tripId,
            @PathVariable Long dayId,
            @PathVariable Long eventId) {
        return ResponseEntity.ok(eventService.getEvent(userId, tripId, dayId, eventId));
    }

    @PutMapping("/{eventId}")
    @Operation(summary = "Обновить событие")
    public ResponseEntity<EventDto> updateEvent(
            @CurrentUser Long userId,
            @PathVariable Long tripId,
            @PathVariable Long dayId,
            @PathVariable Long eventId,
            @RequestBody EventDto eventDto) {
        return ResponseEntity.ok(eventService.updateEvent(userId, tripId, dayId, eventId, eventDto));
    }

    @DeleteMapping("/{eventId}")
    @Operation(summary = "Удалить событие")
    public ResponseEntity<Void> deleteEvent(
            @CurrentUser Long userId,
            @PathVariable Long tripId,
            @PathVariable Long dayId,
            @PathVariable Long eventId) {
        eventService.deleteEvent(userId, tripId, dayId, eventId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{eventId}/reminders")
    @Operation(summary = "Добавить напоминание для события")
    public ResponseEntity<EventReminderDto> addEventReminder(
            @CurrentUser Long userId,
            @PathVariable Long eventId,
            @RequestBody EventReminderDto reminderDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(eventService.addEventReminder(userId, eventId, reminderDto));
    }

    @GetMapping("/{eventId}/reminders")
    @Operation(summary = "Получить список напоминаний для события")
    public ResponseEntity<List<EventReminderDto>> getEventReminders(
            @CurrentUser Long userId,
            @PathVariable Long eventId) {
        return ResponseEntity.ok(eventService.getEventReminders(userId, eventId));
    }

    @DeleteMapping("/{eventId}/reminders/{reminderId}")
    @Operation(summary = "Удалить напоминание для события")
    public ResponseEntity<Void> deleteEventReminder(
            @CurrentUser Long userId,
            @PathVariable Long eventId,
            @PathVariable Long reminderId) {
        eventService.deleteEventReminder(userId, eventId, reminderId);
        return ResponseEntity.noContent().build();
    }
} 