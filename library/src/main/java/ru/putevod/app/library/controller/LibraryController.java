package ru.putevod.app.library.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.putevod.app.library.client.AuthServiceClient;
import ru.putevod.app.library.client.PlannerClient;
import ru.putevod.app.library.dto.PublicRouteDetailDto;
import ru.putevod.app.library.dto.PublicRouteDto;
import ru.putevod.app.library.dto.RoutePreviewDto;
import ru.putevod.app.library.entity.Trip;
import ru.putevod.app.library.security.CurrentUser;
import ru.putevod.app.library.service.LibraryService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/routes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Библиотека маршрутов", description = "API для работы с библиотекой публичных маршрутов")
public class LibraryController {

    private final LibraryService libraryService;
    private final PlannerClient plannerClient;
    private final AuthServiceClient authServiceClient;

    @GetMapping
    @Operation(summary = "Получить список опубликованных маршрутов")
    public ResponseEntity<Page<RoutePreviewDto>> getRoutes(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(libraryService.getPublishedRoutes(pageable));
    }

    @GetMapping("/search")
    @Operation(summary = "Поиск маршрутов по ключевому слову")
    public ResponseEntity<Page<RoutePreviewDto>> searchRoutes(
            @RequestParam String query,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(libraryService.searchRoutes(query, pageable));
    }

    @GetMapping("/filter")
    @Operation(summary = "Фильтрация маршрутов по критериям")
    public ResponseEntity<Page<RoutePreviewDto>> filterRoutes(
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Integer durationMin,
            @RequestParam(required = false) Integer durationMax,
            @RequestParam(required = false) String tag,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(libraryService.getFilteredRoutes(country, city, durationMin, durationMax, tag, pageable));
    }

    @GetMapping("/popular")
    @Operation(summary = "Получить популярные маршруты")
    public ResponseEntity<Page<RoutePreviewDto>> getPopularRoutes(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(libraryService.getPopularRoutes(pageable));
    }

    @GetMapping("/top-rated")
    @Operation(summary = "Получить маршруты с наивысшим рейтингом")
    public ResponseEntity<Page<RoutePreviewDto>> getTopRatedRoutes(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(libraryService.getMostRatedRoutes(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить детальную информацию о маршруте")
    public ResponseEntity<PublicRouteDetailDto> getRouteDetails(
            @PathVariable @Parameter(description = "ID маршрута") Long id) {
        return ResponseEntity.ok(libraryService.getRouteDetails(id));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Получить маршруты опубликованные пользователем")
    public ResponseEntity<List<RoutePreviewDto>> getUserRoutes(
            @PathVariable @Parameter(description = "ID пользователя") Long userId) {
        return ResponseEntity.ok(libraryService.getUserPublishedRoutes(userId));
    }

    @PostMapping("/publish/{tripId}")
    @Operation(summary = "Опубликовать маршрут в библиотеке")
    public ResponseEntity<PublicRouteDto> publishRoute(
            @PathVariable @Parameter(description = "ID маршрута") Long tripId,
            @CurrentUser Long userId,
            Authentication authentication) {
        
        String token = (String) authentication.getCredentials();
        
        if (!plannerClient.canPublishRoute(tripId, userId, token)) {
            return ResponseEntity.badRequest().build();
        }

        Trip trip = plannerClient.getRouteDetails(tripId, token);

        PublicRouteDto publishedRoute = libraryService.publishRoute(trip, userId);
        
        return ResponseEntity.ok(publishedRoute);
    }

    @PutMapping("/approve/{id}")
    @Operation(summary = "Одобрить публикацию маршрута (только для администраторов)")
    public ResponseEntity<PublicRouteDto> approveRoute(
            @PathVariable @Parameter(description = "ID опубликованного маршрута") Long id) {
        return ResponseEntity.ok(libraryService.approvePublishedRoute(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить маршрут из библиотеки")
    public ResponseEntity<Void> deleteRoute(
            @PathVariable @Parameter(description = "ID опубликованного маршрута") Long id) {
        libraryService.deletePublishedRoute(id);
        return ResponseEntity.noContent().build();
    }
} 