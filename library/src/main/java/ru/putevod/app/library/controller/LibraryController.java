package ru.putevod.app.library.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    @Operation(summary = "Получить список опубликованных маршрутов", description = "Возвращает пагинированный список опубликованных и одобренных маршрутов")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Список маршрутов успешно получен",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)))
    })
    public ResponseEntity<Page<RoutePreviewDto>> getRoutes(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(libraryService.getPublishedRoutes(pageable));
    }

    @GetMapping("/pending")
    @Operation(summary = "Получить список неодобренных маршрутов (только для администраторов)", 
            description = "Возвращает пагинированный список маршрутов, ожидающих одобрения (требует прав администратора)",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Список неодобренных маршрутов успешно получен",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
        @ApiResponse(responseCode = "403", description = "Нет прав администратора"),
        @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    public ResponseEntity<Page<RoutePreviewDto>> getPendingRoutes(
            @PageableDefault(size = 20) Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getAuthorities().stream()
                .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(libraryService.getPendingRoutes(pageable));
    }

    @GetMapping("/search")
    @Operation(summary = "Поиск маршрутов по ключевому слову", description = "Выполняет поиск маршрутов по заданному ключевому слову")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Результаты поиска успешно получены",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)))
    })
    public ResponseEntity<Page<RoutePreviewDto>> searchRoutes(
            @RequestParam String query,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(libraryService.searchRoutes(query, pageable));
    }

    @GetMapping("/filter")
    @Operation(summary = "Фильтрация маршрутов по критериям", description = "Фильтрует маршруты по заданным критериям")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Отфильтрованные маршруты успешно получены",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)))
    })
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
    @Operation(summary = "Получить популярные маршруты", description = "Возвращает список наиболее популярных маршрутов")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Популярные маршруты успешно получены",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)))
    })
    public ResponseEntity<Page<RoutePreviewDto>> getPopularRoutes(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(libraryService.getPopularRoutes(pageable));
    }

    @GetMapping("/top-rated")
    @Operation(summary = "Получить маршруты с наивысшим рейтингом", description = "Возвращает список маршрутов с наивысшими оценками")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Маршруты с высоким рейтингом успешно получены",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)))
    })
    public ResponseEntity<Page<RoutePreviewDto>> getTopRatedRoutes(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(libraryService.getMostRatedRoutes(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить детальную информацию о маршруте", description = "Возвращает подробную информацию о маршруте по его ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Детали маршрута успешно получены",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = PublicRouteDetailDto.class))),
        @ApiResponse(responseCode = "404", description = "Маршрут не найден")
    })
    public ResponseEntity<PublicRouteDetailDto> getRouteDetails(
            @PathVariable @Parameter(description = "ID маршрута") Long id) {
        return ResponseEntity.ok(libraryService.getRouteDetails(id));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Получить маршруты опубликованные пользователем", description = "Возвращает список маршрутов, опубликованных указанным пользователем")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Маршруты пользователя успешно получены",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class))),
        @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    public ResponseEntity<List<RoutePreviewDto>> getUserRoutes(
            @PathVariable @Parameter(description = "ID пользователя") Long userId) {
        return ResponseEntity.ok(libraryService.getUserPublishedRoutes(userId));
    }

    @PostMapping("/publish/{tripId}")
    @Operation(summary = "Опубликовать маршрут в библиотеке", description = "Публикует маршрут в библиотеке маршрутов",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Маршрут успешно опубликован",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = PublicRouteDto.class))),
        @ApiResponse(responseCode = "400", description = "Ошибка публикации маршрута"),
        @ApiResponse(responseCode = "403", description = "Нет доступа к маршруту"),
        @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    public ResponseEntity<PublicRouteDto> publishRoute(
            @PathVariable @Parameter(description = "ID маршрута") Long tripId,
            @CurrentUser Long userId,
            Authentication authentication) {
        if (tripId <= 0) {
            return ResponseEntity.badRequest().build();
        }
        
        String token = (String) authentication.getCredentials();
        
        if (!plannerClient.canPublishRoute(tripId, userId, token)) {
            return ResponseEntity.badRequest().build();
        }

        Trip trip = plannerClient.getRouteDetails(tripId, token);
        
        plannerClient.publishRoute(tripId, userId, token, true);

        PublicRouteDto publishedRoute = libraryService.publishRoute(trip, userId);
        
        return ResponseEntity.ok(publishedRoute);
    }

    @PutMapping("/approve/{id}")
    @Operation(summary = "Одобрить публикацию маршрута (только для администраторов)", 
            description = "Одобряет публикацию маршрута в библиотеке (требует прав администратора)",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Маршрут успешно одобрен",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = PublicRouteDto.class))),
        @ApiResponse(responseCode = "404", description = "Маршрут не найден"),
        @ApiResponse(responseCode = "403", description = "Нет прав администратора"),
        @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
public ResponseEntity<PublicRouteDto> approveRoute(
        @PathVariable @Parameter(description = "ID опубликованного маршрута") Long id) {
   Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

   if (authentication == null || authentication.getAuthorities().stream()
           .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
       return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
   }
   
    return ResponseEntity.ok(libraryService.approvePublishedRoute(id));
}

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить маршрут из библиотеки", 
            description = "Удаляет маршрут из библиотеки (требуется авторизация)",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Маршрут успешно удален"),
        @ApiResponse(responseCode = "404", description = "Маршрут не найден"),
        @ApiResponse(responseCode = "403", description = "Нет прав на удаление"),
        @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    public ResponseEntity<Void> deleteRoute(
            @PathVariable @Parameter(description = "ID опубликованного маршрута") Long id,
            @CurrentUser Long userId,
            Authentication authentication) {
        
        var publishedRoute = libraryService.getPublishedRouteById(id);
        Long originalRouteId = publishedRoute.getOriginalRouteId();
        
        libraryService.deletePublishedRoute(id);
        
        String token = (String) authentication.getCredentials();
        plannerClient.publishRoute(originalRouteId, userId, token, false);
        
        return ResponseEntity.noContent().build();
    }
} 