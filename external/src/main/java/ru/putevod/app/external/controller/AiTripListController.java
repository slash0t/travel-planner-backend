package ru.putevod.app.external.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.putevod.app.external.exception.ServiceUnavailableException;
import ru.putevod.app.external.service.AiTripListService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai/trip-lists")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI Trip Lists", description = "API для генерации списков для поездки с помощью искусственного интеллекта")
public class AiTripListController {
    private final AiTripListService aiTripListService;

    @PostMapping("/generate")
    @Operation(summary = "Генерация списка для поездки на основе текстового запроса",
            description = "Генерирует список элементов для поездки на основе текстового описания")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список успешно сгенерирован"),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос или небезопасный запрос"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера"),
            @ApiResponse(responseCode = "503", description = "Сервис временно недоступен")
    })
    public ResponseEntity<Object> generateTripList(
            @Parameter(description = "Текстовое описание поездки") @RequestParam String prompt,
            @Parameter(description = "Длительность поездки в днях") @RequestParam(required = false) Integer duration,
            @Parameter(description = "Место назначения") @RequestParam(required = false) String destination,
            @Parameter(description = "Сезон (зима, весна, лето, осень)") @RequestParam(required = false) String season) {

        log.info("Запрос на генерацию списка для поездки: prompt={}, duration={}, destination={}, season={}",
                prompt, duration, destination, season);

        if (prompt == null || prompt.trim().isEmpty()) {
            return createErrorResponse(HttpStatus.BAD_REQUEST, "Ошибка", "Запрос не может быть пустым");
        }

        if (!aiTripListService.isSafePrompt(prompt)) {
            log.warn("Обнаружен небезопасный запрос: {}", prompt);
            return createErrorResponse(HttpStatus.BAD_REQUEST, "Ошибка безопасности", "Запрос содержит запрещенную тематику");
        }

        Map<String, Object> context = new HashMap<>();
        if (duration != null) {
            context.put("duration", duration);
        }
        if (destination != null && !destination.isEmpty()) {
            context.put("destination", destination);
        }
        if (season != null && !season.isEmpty()) {
            context.put("season", season);
        }

        try {
            List<String> items = aiTripListService.generateTripListFromPrompt(prompt, context);

            if (items.size() == 1 && items.get(0).startsWith("Ошибка:")) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "Ошибка генерации", items.get(0));
            }

            return ResponseEntity.ok(items);
        } catch (ServiceUnavailableException e) {
            log.error("Сервис недоступен: {}", e.getMessage());
            return createErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, "Сервис недоступен", e.getMessage());
        } catch (Exception e) {
            log.error("Ошибка при генерации списка: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера", "Произошла ошибка при обработке запроса");
        }
    }

    @PostMapping("/generate/from-trip/{tripId}")
    @Operation(summary = "Генерация списка на основе существующей поездки",
            description = "Генерирует список элементов на основе информации о существующей поездке")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список успешно сгенерирован"),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            @ApiResponse(responseCode = "404", description = "Поездка не найдена"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера"),
            @ApiResponse(responseCode = "503", description = "Сервис временно недоступен")
    })
    public ResponseEntity<Object> generateTripListFromTrip(
            @Parameter(description = "ID поездки") @PathVariable Long tripId,
            @Parameter(description = "Дополнительный запрос") @RequestParam(required = false) String additionalPrompt) {

        log.info("Запрос на генерацию списка для поездки по ID: tripId={}, additionalPrompt={}",
                tripId, additionalPrompt);

        if (tripId == null || tripId <= 0) {
            return createErrorResponse(HttpStatus.BAD_REQUEST, "Ошибка", "ID поездки должен быть положительным числом");
        }

        Map<String, Object> context = new HashMap<>();
        if (additionalPrompt != null && !additionalPrompt.isEmpty()) {
            context.put("additionalInfo", additionalPrompt);

            if (!aiTripListService.isSafePrompt(additionalPrompt)) {
                log.warn("Обнаружен небезопасный дополнительный запрос: {}", additionalPrompt);
                return createErrorResponse(HttpStatus.BAD_REQUEST, "Ошибка безопасности", "Дополнительный запрос содержит запрещенную тематику");
            }
        }

        try {
            List<String> items = aiTripListService.generateTripListFromTrip(tripId, context);

            if (items.size() == 1 && (items.get(0).startsWith("Ошибка:") || items.get(0).startsWith("Не удалось получить информацию о поездке"))) {
                return createErrorResponse(HttpStatus.NOT_FOUND, "Поездка не найдена", items.get(0));
            }

            return ResponseEntity.ok(items);
        } catch (ServiceUnavailableException e) {
            log.error("Сервис недоступен: {}", e.getMessage());
            return createErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, "Сервис недоступен", e.getMessage());
        } catch (Exception e) {
            log.error("Ошибка при генерации списка: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера", "Произошла ошибка при обработке запроса");
        }
    }

    @PostMapping("/generate/from-template/{templateId}")
    @Operation(summary = "Генерация списка на основе шаблона",
            description = "Генерирует список элементов на основе существующего шаблона и дополнительного контекста")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список успешно сгенерирован"),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            @ApiResponse(responseCode = "404", description = "Шаблон не найден"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера"),
            @ApiResponse(responseCode = "503", description = "Сервис временно недоступен")
    })
    public ResponseEntity<Object> generateTripListFromTemplate(
            @Parameter(description = "ID шаблона") @PathVariable Long templateId,
            @Parameter(description = "Длительность поездки в днях") @RequestParam(required = false) Integer duration,
            @Parameter(description = "Место назначения") @RequestParam(required = false) String destination,
            @Parameter(description = "Сезон (зима, весна, лето, осень)") @RequestParam(required = false) String season,
            @Parameter(description = "Дополнительный запрос") @RequestParam(required = false) String additionalPrompt) {

        log.info("Запрос на генерацию списка для поездки по шаблону: templateId={}, duration={}, destination={}, season={}, additionalPrompt={}",
                templateId, duration, destination, season, additionalPrompt);

        if (templateId == null || templateId <= 0) {
            return createErrorResponse(HttpStatus.BAD_REQUEST, "Ошибка", "ID шаблона должен быть положительным числом");
        }

        Map<String, Object> context = new HashMap<>();
        if (duration != null) {
            context.put("duration", duration);
        }
        if (destination != null && !destination.isEmpty()) {
            context.put("destination", destination);
        }
        if (season != null && !season.isEmpty()) {
            context.put("season", season);
        }

        if (additionalPrompt != null && !additionalPrompt.isEmpty()) {
            context.put("additionalInfo", additionalPrompt);

            if (!aiTripListService.isSafePrompt(additionalPrompt)) {
                log.warn("Обнаружен небезопасный дополнительный запрос: {}", additionalPrompt);
                return createErrorResponse(HttpStatus.BAD_REQUEST, "Ошибка безопасности", "Дополнительный запрос содержит запрещенную тематику");
            }
        }

        try {
            List<String> items = aiTripListService.generateTripListFromTemplate(templateId, context);

            if (items.size() == 1 && (items.get(0).startsWith("Ошибка:") || items.get(0).startsWith("Не удалось получить информацию о шаблоне"))) {
                return createErrorResponse(HttpStatus.NOT_FOUND, "Шаблон не найден", items.get(0));
            }

            return ResponseEntity.ok(items);
        } catch (ServiceUnavailableException e) {
            log.error("Сервис недоступен: {}", e.getMessage());
            return createErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, "Сервис недоступен", e.getMessage());
        } catch (Exception e) {
            log.error("Ошибка при генерации списка: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера", "Произошла ошибка при обработке запроса");
        }
    }

    private ResponseEntity<Object> createErrorResponse(HttpStatus status, String error, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", status.value());
        response.put("error", error);
        response.put("message", message);
        return new ResponseEntity<>(response, status);
    }
} 