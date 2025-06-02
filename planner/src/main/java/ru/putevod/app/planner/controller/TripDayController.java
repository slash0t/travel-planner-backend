package ru.putevod.app.planner.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.putevod.app.planner.config.CurrentUser;
import ru.putevod.app.planner.dto.TripDayDto;
import ru.putevod.app.planner.service.TripDayService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/trips/{tripId}/days")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Trip Days", description = "API для управления днями поездок")
@SecurityRequirement(name = "bearerAuth")
public class TripDayController {
    private final TripDayService tripDayService;

    @PostMapping
    @Operation(summary = "Добавить день в поездку")
    public ResponseEntity<TripDayDto> createTripDay(
            @CurrentUser Long userId,
            @PathVariable Long tripId,
            @Valid @RequestBody TripDayDto tripDayDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tripDayService.createTripDay(userId, tripId, tripDayDto));
    }

    @GetMapping
    @Operation(summary = "Получить все дни поездки")
    public ResponseEntity<List<TripDayDto>> getTripDays(
            @CurrentUser Long userId,
            @PathVariable Long tripId) {
        return ResponseEntity.ok(tripDayService.getTripDays(userId, tripId));
    }

    @GetMapping("/{dayId}")
    @Operation(summary = "Получить день поездки по ID")
    public ResponseEntity<TripDayDto> getTripDay(
            @CurrentUser Long userId,
            @PathVariable Long tripId,
            @PathVariable Long dayId) {
        return ResponseEntity.ok(tripDayService.getTripDay(userId, tripId, dayId));
    }

    @GetMapping("/date")
    @Operation(summary = "Получить день поездки по дате")
    public ResponseEntity<TripDayDto> getTripDayByDate(
            @CurrentUser Long userId,
            @PathVariable Long tripId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(tripDayService.getTripDayByDate(userId, tripId, date));
    }

    @GetMapping("/number/{dayNumber}")
    @Operation(summary = "Получить день поездки по номеру дня")
    public ResponseEntity<TripDayDto> getTripDayByNumber(
            @CurrentUser Long userId,
            @PathVariable Long tripId,
            @PathVariable Integer dayNumber) {
        List<TripDayDto> days = tripDayService.getTripDays(userId, tripId);
        TripDayDto dayDto = days.stream()
                .filter(day -> dayNumber.equals(day.getDayNumber()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("День с номером " + dayNumber + " не найден"));
        return ResponseEntity.ok(dayDto);
    }

    @GetMapping("/current")
    @Operation(summary = "Получить текущий день поездки")
    public ResponseEntity<TripDayDto> getCurrentTripDay(
            @CurrentUser Long userId,
            @PathVariable Long tripId) {
        try {
            return ResponseEntity.ok(tripDayService.getTripDayByDate(userId, tripId, LocalDate.now()));
        } catch (Exception e) {
            List<TripDayDto> days = tripDayService.getTripDays(userId, tripId);
            if (!days.isEmpty()) {
                return ResponseEntity.ok(days.get(0));
            }
            throw new RuntimeException("Дни поездки не найдены");
        }
    }

    @PutMapping("/{dayId}")
    @Operation(summary = "Обновить день поездки")
    public ResponseEntity<TripDayDto> updateTripDay(
            @CurrentUser Long userId,
            @PathVariable Long tripId,
            @PathVariable Long dayId,
            @Valid @RequestBody TripDayDto tripDayDto) {
        return ResponseEntity.ok(tripDayService.updateTripDay(userId, tripId, dayId, tripDayDto));
    }

    @DeleteMapping("/{dayId}")
    @Operation(summary = "Удалить день поездки")
    public ResponseEntity<Void> deleteTripDay(
            @CurrentUser Long userId,
            @PathVariable Long tripId,
            @PathVariable Long dayId) {
        tripDayService.deleteTripDay(userId, tripId, dayId);
        return ResponseEntity.noContent().build();
    }
} 