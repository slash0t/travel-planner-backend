package ru.putevod.app.planner.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.putevod.app.planner.config.CurrentUser;
import ru.putevod.app.planner.dto.TripAccessDto;
import ru.putevod.app.planner.dto.TripDto;
import ru.putevod.app.planner.service.TripService;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Trips", description = "API для управления поездками")
public class TripController {
    private final TripService tripService;
    
    @PostMapping
    @Operation(summary = "Создать новую поездку")
    public ResponseEntity<TripDto> createTrip(
            @CurrentUser Long userId,
            @RequestBody TripDto tripDto) {
        log.info("Creating trip: {} for user: {}", tripDto, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tripService.createTrip(userId, tripDto));
    }
    
    @GetMapping
    @Operation(summary = "Получить список поездок пользователя")
    public ResponseEntity<Page<TripDto>> getUserTrips(
            @CurrentUser Long userId,
            @RequestParam(required = false, defaultValue = "all") String filter,
            Pageable pageable) {
        return ResponseEntity.ok(tripService.getUserTrips(userId, filter, pageable));
    }
    
    @GetMapping("/upcoming")
    @Operation(summary = "Получить предстоящие поездки")
    public ResponseEntity<List<TripDto>> getUpcomingTrips(
            @CurrentUser Long userId) {
        return ResponseEntity.ok(tripService.getUpcomingTrips(userId));
    }
    
    @GetMapping("/ongoing")
    @Operation(summary = "Получить текущие поездки")
    public ResponseEntity<List<TripDto>> getOngoingTrips(
            @CurrentUser Long userId) {
        return ResponseEntity.ok(tripService.getOngoingTrips(userId));
    }
    
    @GetMapping("/past")
    @Operation(summary = "Получить прошедшие поездки")
    public ResponseEntity<List<TripDto>> getPastTrips(
            @CurrentUser Long userId) {
        return ResponseEntity.ok(tripService.getPastTrips(userId));
    }
    
    @GetMapping("/{tripId}")
    @Operation(summary = "Получить поездку по ID")
    public ResponseEntity<TripDto> getTripById(
            @CurrentUser Long userId,
            @PathVariable Long tripId) {
        return ResponseEntity.ok(tripService.getTripById(userId, tripId));
    }
    
    @PutMapping("/{tripId}")
    @Operation(summary = "Обновить поездку")
    public ResponseEntity<TripDto> updateTrip(
            @CurrentUser Long userId,
            @PathVariable Long tripId,
            @RequestBody TripDto tripDto) {
        return ResponseEntity.ok(tripService.updateTrip(userId, tripId, tripDto));
    }
    
    @DeleteMapping("/{tripId}")
    @Operation(summary = "Удалить поездку")
    public ResponseEntity<Void> deleteTrip(
            @CurrentUser Long userId,
            @PathVariable Long tripId) {
        tripService.deleteTrip(userId, tripId);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{tripId}/share")
    @Operation(summary = "Предоставить доступ к поездке")
    public ResponseEntity<TripAccessDto> shareTrip(
            @CurrentUser Long userId,
            @PathVariable Long tripId,
            @RequestBody TripAccessDto accessDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tripService.shareTrip(userId, tripId, accessDto));
    }
    
    @GetMapping("/{tripId}/shares")
    @Operation(summary = "Получить список пользователей с доступом к поездке")
    public ResponseEntity<List<TripAccessDto>> getTripShares(
            @CurrentUser Long userId,
            @PathVariable Long tripId) {
        return ResponseEntity.ok(tripService.getTripShares(userId, tripId));
    }
    
    @DeleteMapping("/{tripId}/shares/{shareUserId}")
    @Operation(summary = "Удалить доступ к поездке для пользователя")
    public ResponseEntity<Void> removeShare(
            @CurrentUser Long userId,
            @PathVariable Long tripId,
            @PathVariable Long shareUserId) {
        tripService.removeShare(userId, tripId, shareUserId);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{tripId}/invitation")
    @Operation(summary = "Ответить на приглашение в поездку")
    public ResponseEntity<TripAccessDto> respondToInvitation(
            @CurrentUser Long userId,
            @PathVariable Long tripId,
            @RequestParam String status) {
        return ResponseEntity.ok(tripService.respondToInvitation(userId, tripId, status));
    }
} 