package ru.putevod.app.external.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.putevod.app.external.dto.PlaceResponseDto;
import ru.putevod.app.external.dto.response.PlaceSearchResponse;
import ru.putevod.app.external.dto.response.PlaceSuggestionResponse;
import ru.putevod.app.external.service.PlaceService;
import ru.putevod.app.library.annotation.TrackMetrics;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/places")
@RequiredArgsConstructor
@Tag(name = "Places", description = "API для работы с информацией о местах")
@SecurityRequirement(name = "bearerAuth")
public class PlaceController {

    private final PlaceService placeService;

    @GetMapping("/search")
    @TrackMetrics(value = "search_places", type = TrackMetrics.EventType.EXTERNAL)
    @Operation(summary = "Поиск мест", description = "Поиск мест по заданному запросу с возможностью фильтрации по местоположению и категориям")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешный поиск",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PlaceSearchResponse.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса"),
            @ApiResponse(responseCode = "401", description = "Неавторизованный запрос"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    public ResponseEntity<PlaceSearchResponse> searchPlaces(
            @Parameter(description = "Поисковый запрос") @RequestParam String query,
            @Parameter(description = "Широта (для поиска рядом с точкой)") @RequestParam(required = false) Double lat,
            @Parameter(description = "Долгота (для поиска рядом с точкой)") @RequestParam(required = false) Double lon,
            @Parameter(description = "Радиус поиска в метрах") @RequestParam(required = false, defaultValue = "5000") Integer radius,
            @Parameter(description = "Максимальное количество результатов") @RequestParam(required = false, defaultValue = "20") Integer limit,
            @Parameter(description = "Категория места") @RequestParam(required = false) String category) {

        return ResponseEntity.ok(placeService.searchPlaces(query, lat, lon, radius, limit, category));
    }

    @GetMapping("/{placeId}")
    @TrackMetrics(value = "get_place_details", type = TrackMetrics.EventType.EXTERNAL)
    @Operation(summary = "Получить детальную информацию о месте", description = "Возвращает подробную информацию о месте по его ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Информация о месте успешно получена",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PlaceResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Место не найдено"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    public ResponseEntity<PlaceResponseDto> getPlaceDetails(
            @Parameter(description = "ID места") @PathVariable String placeId) {
        return ResponseEntity.ok(placeService.getPlaceDetails(placeId));
    }

    @GetMapping("/autocomplete")
    @TrackMetrics(value = "autocomplete_places", type = TrackMetrics.EventType.EXTERNAL)
    @Operation(summary = "Автодополнение для поиска мест", description = "Предлагает варианты завершения поискового запроса")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Варианты автодополнения успешно получены",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PlaceSuggestionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    public ResponseEntity<PlaceSuggestionResponse> autocompletePlaces(
            @Parameter(description = "Начало поискового запроса") @RequestParam String input,
            @Parameter(description = "Широта (для поиска рядом с точкой)") @RequestParam(required = false) Double lat,
            @Parameter(description = "Долгота (для поиска рядом с точкой)") @RequestParam(required = false) Double lon,
            @Parameter(description = "Максимальное количество результатов") @RequestParam(required = false, defaultValue = "5") Integer limit) {

        return ResponseEntity.ok(placeService.autocompletePlaces(input, lat, lon, limit));
    }

    @GetMapping("/geocode")
    @TrackMetrics(value = "geocode_address", type = TrackMetrics.EventType.EXTERNAL)
    @Operation(summary = "Геокодирование адреса", description = "Получение координат по адресу или названию места")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Координаты успешно получены",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса"),
            @ApiResponse(responseCode = "404", description = "Адрес не найден"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    public ResponseEntity<Map<String, Double>> geocodeAddress(
            @Parameter(description = "Адрес или название места") @RequestParam String address) {

        Map<String, Double> coordinates = placeService.geocodeAddress(address);
        if (coordinates == null || coordinates.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(coordinates);
    }

    @GetMapping("/nearby")
    @TrackMetrics(value = "get_nearby_places", type = TrackMetrics.EventType.EXTERNAL)
    @Operation(summary = "Найти ближайшие места", description = "Поиск мест вокруг заданной точки")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ближайшие места успешно найдены",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PlaceSearchResponse.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    public ResponseEntity<PlaceSearchResponse> getNearbyPlaces(
            @Parameter(description = "Широта") @RequestParam Double lat,
            @Parameter(description = "Долгота") @RequestParam Double lon,
            @Parameter(description = "Радиус поиска в метрах") @RequestParam(required = false, defaultValue = "1000") Integer radius,
            @Parameter(description = "Максимальное количество результатов") @RequestParam(required = false, defaultValue = "20") Integer limit,
            @Parameter(description = "Категории мест (через запятую)") @RequestParam(required = false) String categories) {

        return ResponseEntity.ok(placeService.getNearbyPlaces(lat, lon, radius, limit, categories));
    }
} 