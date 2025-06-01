package ru.putevod.app.external.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.putevod.app.external.dto.ai.PackingListRequest;
import ru.putevod.app.external.dto.ai.PackingListResponse;
import ru.putevod.app.external.dto.ai.PackingListTemplateContent;
import ru.putevod.app.external.dto.ai.PackingListTemplatesResponse;
import ru.putevod.app.external.security.CurrentUser;
import ru.putevod.app.external.service.AiService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI Services", description = "API для взаимодействия с искусственным интеллектом")
@SecurityRequirement(name = "bearerAuth")
public class AiController {

    private final AiService aiService;

    @PostMapping("/packing-list")
    @Operation(summary = "Генерация списка вещей для поездки",
            description = "Создает персонализированный список вещей для поездки на основе параметров запроса")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список вещей успешно сгенерирован",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PackingListResponse.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса"),
            @ApiResponse(responseCode = "401", description = "Неавторизованный запрос"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    public ResponseEntity<PackingListResponse> generatePackingList(
            @Parameter(description = "Параметры для генерации списка вещей", required = true)
            @Valid @RequestBody PackingListRequest request,
            @CurrentUser(info = true) Map<String, Object> userInfo) {
        return ResponseEntity.ok(aiService.generatePackingList(request));
    }

    @GetMapping("/packing-list/templates")
    @Operation(summary = "Получение шаблонов списков вещей",
            description = "Возвращает список доступных шаблонов для создания списка вещей")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Шаблоны успешно получены",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PackingListTemplatesResponse.class))),
            @ApiResponse(responseCode = "401", description = "Неавторизованный запрос"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    public ResponseEntity<PackingListTemplatesResponse> getPackingListTemplates(
            @CurrentUser(info = true) Map<String, Object> userInfo) {
        return ResponseEntity.ok(aiService.getPackingListTemplates());
    }

    @GetMapping("/packing-list/template/{templateId}")
    @Operation(summary = "Получение содержимого шаблона списка вещей",
            description = "Возвращает содержимое конкретного шаблона списка вещей по его ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Содержимое шаблона успешно получено",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PackingListTemplateContent.class))),
            @ApiResponse(responseCode = "404", description = "Шаблон не найден"),
            @ApiResponse(responseCode = "401", description = "Неавторизованный запрос"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    public ResponseEntity<PackingListTemplateContent> getPackingListTemplateContent(
            @Parameter(description = "ID шаблона", required = true)
            @PathVariable String templateId,
            @CurrentUser(info = true) Map<String, Object> userInfo) {
        return ResponseEntity.ok(aiService.getPackingListTemplateContent(templateId));
    }
} 