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
    @Operation(summary = "Универсальная генерация списка для поездки",
            description = "Генерирует список элементов для поездки на основе: текстового запроса, существующей поездки с промптом, или шаблона с промптом и параметрами")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список успешно сгенерирован"),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос или небезопасный запрос"),
            @ApiResponse(responseCode = "404", description = "Поездка или шаблон не найдены"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера"),
            @ApiResponse(responseCode = "503", description = "Сервис временно недоступен")
    })
    public ResponseEntity<Object> generate(
            @Parameter(description = "Текстовое описание поездки (обязательно для генерации с нуля, опционально для tripId и templateId)") 
            @RequestParam(required = false) String prompt,
            @Parameter(description = "ID существующей поездки для генерации на её основе") 
            @RequestParam(required = false) Long tripId,
            @Parameter(description = "ID шаблона для генерации на его основе") 
            @RequestParam(required = false) Long templateId,
            @Parameter(description = "Длительность поездки в днях") 
            @RequestParam(required = false) Integer duration,
            @Parameter(description = "Место назначения") 
            @RequestParam(required = false) String destination,
            @Parameter(description = "Сезон (зима, весна, лето, осень)") 
            @RequestParam(required = false) String season,
            @Parameter(description = "Дополнительный запрос или контекст") 
            @RequestParam(required = false) String additionalPrompt) {

        log.info("Универсальный запрос на генерацию списка: prompt={}, tripId={}, templateId={}, duration={}, destination={}, season={}, additionalPrompt={}",
                prompt, tripId, templateId, duration, destination, season, additionalPrompt);

        boolean hasTripId = tripId != null && tripId > 0;
        boolean hasTemplateId = templateId != null && templateId > 0;
        boolean hasPrompt = prompt != null && !prompt.trim().isEmpty();

        if (hasTripId && hasTemplateId) {
            return createErrorResponse(HttpStatus.BAD_REQUEST, "Ошибка", 
                "Нельзя одновременно указывать tripId и templateId");
        }

        if (!hasTripId && !hasTemplateId && !hasPrompt) {
            return createErrorResponse(HttpStatus.BAD_REQUEST, "Ошибка", 
                "Необходимо указать: prompt (для генерации с нуля), tripId (для генерации по поездке), или templateId (для генерации по шаблону)");
        }

        if (hasPrompt && !aiTripListService.isSafePrompt(prompt)) {
            log.warn("Обнаружен небезопасный запрос: {}", prompt);
            return createErrorResponse(HttpStatus.BAD_REQUEST, "Ошибка безопасности", "Запрос содержит запрещенную тематику");
        }

        if (additionalPrompt != null && !additionalPrompt.trim().isEmpty() && !aiTripListService.isSafePrompt(additionalPrompt)) {
            log.warn("Обнаружен небезопасный дополнительный запрос: {}", additionalPrompt);
            return createErrorResponse(HttpStatus.BAD_REQUEST, "Ошибка безопасности", "Дополнительный запрос содержит запрещенную тематику");
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
        }
       if ((hasTripId || hasTemplateId) && hasPrompt) {
            context.put("prompt", prompt);
        }

        try {
            List<String> items;

           if (hasTripId) {
                items = aiTripListService.generateTripListFromTrip(tripId, context);
            } else if (hasTemplateId) {
                items = aiTripListService.generateTripListFromTemplate(templateId, context);
            } else {
               items = aiTripListService.generateTripListFromPrompt(prompt, context);
            }

            if (items.size() == 1) {
                String errorMessage = items.get(0);
                if (errorMessage.startsWith("Ошибка:")) {
                    return createErrorResponse(HttpStatus.BAD_REQUEST, "Ошибка генерации", errorMessage);
                }
                if (errorMessage.startsWith("Не удалось получить информацию о поездке")) {
                    return createErrorResponse(HttpStatus.NOT_FOUND, "Поездка не найдена", errorMessage);
                }
                if (errorMessage.startsWith("Не удалось получить информацию о шаблоне")) {
                    return createErrorResponse(HttpStatus.NOT_FOUND, "Шаблон не найден", errorMessage);
                }
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