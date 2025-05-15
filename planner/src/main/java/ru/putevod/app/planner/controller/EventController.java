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
import ru.putevod.app.planner.dto.EventDto;
import ru.putevod.app.planner.dto.EventReminderDto;
import ru.putevod.app.planner.service.EventService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trips/{tripId}/days/{dayId}/places")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Places", description = "API для управления местами и событиями")
@SecurityRequirement(name = "bearerAuth")
public class EventController {
    private final EventService eventService;
    
    @PostMapping
    @Operation(summary = "Создать место/событие")
    public ResponseEntity<EventDto> createEvent(
            @CurrentUser Long userId,
            @PathVariable Long tripId,
            @PathVariable Long dayId,
            @RequestBody EventDto eventDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(eventService.createEvent(userId, tripId, dayId, eventDto));
    }
    
    @GetMapping
    @Operation(summary = "Получить все места/события дня")
    public ResponseEntity<List<EventDto>> getDayEvents(
            @CurrentUser Long userId,
            @PathVariable Long tripId,
            @PathVariable Long dayId) {
        return ResponseEntity.ok(eventService.getDayEvents(userId, tripId, dayId));
    }
    
    @GetMapping("/{placeId}")
    @Operation(summary = "Получить место/событие по ID")
    public ResponseEntity<EventDto> getEvent(
            @CurrentUser Long userId,
            @PathVariable Long tripId,
            @PathVariable Long dayId,
            @PathVariable("placeId") Long eventId) {
        return ResponseEntity.ok(eventService.getEvent(userId, tripId, dayId, eventId));
    }
    
    @PutMapping("/{placeId}")
    @Operation(summary = "Обновить место/событие")
    public ResponseEntity<EventDto> updateEvent(
            @CurrentUser Long userId,
            @PathVariable Long tripId,
            @PathVariable Long dayId,
            @PathVariable("placeId") Long eventId,
            @RequestBody EventDto eventDto) {
        return ResponseEntity.ok(eventService.updateEvent(userId, tripId, dayId, eventId, eventDto));
    }
    
    @DeleteMapping("/{placeId}")
    @Operation(summary = "Удалить место/событие")
    public ResponseEntity<Void> deleteEvent(
            @CurrentUser Long userId,
            @PathVariable Long tripId,
            @PathVariable Long dayId,
            @PathVariable("placeId") Long eventId) {
        eventService.deleteEvent(userId, tripId, dayId, eventId);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{placeId}/reminders")
    @Operation(summary = "Добавить напоминание для места/события")
    public ResponseEntity<EventReminderDto> addEventReminder(
            @CurrentUser Long userId,
            @PathVariable("placeId") Long eventId,
            @RequestBody EventReminderDto reminderDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(eventService.addEventReminder(userId, eventId, reminderDto));
    }
    
    @GetMapping("/{placeId}/reminders")
    @Operation(summary = "Получить список напоминаний для места/события")
    public ResponseEntity<List<EventReminderDto>> getEventReminders(
            @CurrentUser Long userId,
            @PathVariable("placeId") Long eventId) {
        return ResponseEntity.ok(eventService.getEventReminders(userId, eventId));
    }
    
    @DeleteMapping("/{placeId}/reminders/{reminderId}")
    @Operation(summary = "Удалить напоминание для места/события")
    public ResponseEntity<Void> deleteEventReminder(
            @CurrentUser Long userId,
            @PathVariable("placeId") Long eventId,
            @PathVariable Long reminderId) {
        eventService.deleteEventReminder(userId, eventId, reminderId);
        return ResponseEntity.noContent().build();
    }
} 