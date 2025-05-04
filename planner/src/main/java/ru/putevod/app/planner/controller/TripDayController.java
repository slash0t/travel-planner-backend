package ru.putevod.app.planner.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.putevod.app.planner.dto.TripDayDto;
import ru.putevod.app.planner.service.TripDayService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/trips/{tripId}/days")
@RequiredArgsConstructor
@Tag(name = "Trip Days", description = "API для управления днями поездок")
public class TripDayController {
    private final TripDayService tripDayService;
    
    @PostMapping
    @Operation(summary = "Добавить день в поездку")
    public ResponseEntity<TripDayDto> createTripDay(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long tripId,
            @RequestBody TripDayDto tripDayDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tripDayService.createTripDay(userId, tripId, tripDayDto));
    }
    
    @GetMapping
    @Operation(summary = "Получить все дни поездки")
    public ResponseEntity<List<TripDayDto>> getTripDays(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long tripId) {
        return ResponseEntity.ok(tripDayService.getTripDays(userId, tripId));
    }
    
    @GetMapping("/{dayId}")
    @Operation(summary = "Получить день поездки по ID")
    public ResponseEntity<TripDayDto> getTripDay(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long tripId,
            @PathVariable Long dayId) {
        return ResponseEntity.ok(tripDayService.getTripDay(userId, tripId, dayId));
    }
    
    @GetMapping("/date")
    @Operation(summary = "Получить день поездки по дате")
    public ResponseEntity<TripDayDto> getTripDayByDate(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long tripId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(tripDayService.getTripDayByDate(userId, tripId, date));
    }
    
    @PutMapping("/{dayId}")
    @Operation(summary = "Обновить день поездки")
    public ResponseEntity<TripDayDto> updateTripDay(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long tripId,
            @PathVariable Long dayId,
            @RequestBody TripDayDto tripDayDto) {
        return ResponseEntity.ok(tripDayService.updateTripDay(userId, tripId, dayId, tripDayDto));
    }
    
    @DeleteMapping("/{dayId}")
    @Operation(summary = "Удалить день поездки")
    public ResponseEntity<Void> deleteTripDay(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long tripId,
            @PathVariable Long dayId) {
        tripDayService.deleteTripDay(userId, tripId, dayId);
        return ResponseEntity.noContent().build();
    }
} 