package ru.putevod.app.planner.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.putevod.app.planner.dto.TemplateItemDto;
import ru.putevod.app.planner.dto.TodoTemplateDto;
import ru.putevod.app.planner.model.TemplateItem;
import ru.putevod.app.planner.model.TodoTemplate;
import ru.putevod.app.planner.repository.TemplateItemRepository;
import ru.putevod.app.planner.repository.TodoTemplateRepository;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/templates")
@RequiredArgsConstructor
@Tag(name = "Todo Templates", description = "API для управления шаблонами задач. Позволяет получать готовые шаблоны задач для различных категорий поездок.")
public class TodoTemplateController {

    private final TodoTemplateRepository todoTemplateRepository;
    private final TemplateItemRepository templateItemRepository;

    @Operation(
            summary = "Получить все шаблоны задач",
            description = "Возвращает список всех доступных шаблонов задач с их элементами. Шаблоны содержат готовые наборы задач для различных типов поездок (деловые, туристические, экстремальные и др.)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Список шаблонов успешно получен",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TodoTemplateDto.class)
                    )
            ),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @GetMapping
    public ResponseEntity<List<TodoTemplateDto>> getAllTemplates() {
        List<TodoTemplate> templates = todoTemplateRepository.findAll();

        List<TodoTemplateDto> result = templates.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "Получить детали шаблона",
            description = "Возвращает подробную информацию о конкретном шаблоне задач включая все его элементы в правильном порядке."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Детали шаблона успешно получены",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TodoTemplateDto.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Шаблон с указанным ID не найден"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @GetMapping("/{templateId}")
    public ResponseEntity<TodoTemplateDto> getTemplateDetails(
            @Parameter(description = "Уникальный идентификатор шаблона", required = true, example = "1")
            @PathVariable Long templateId) {
        return todoTemplateRepository.findById(templateId)
                .map(template -> ResponseEntity.ok(convertToDto(template)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Получить элементы шаблона",
            description = "Возвращает список всех задач (элементов) конкретного шаблона отсортированный по позиции. Используется для быстрого получения только содержимого задач без метаданных шаблона."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Элементы шаблона успешно получены",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "array", implementation = String.class, example = "[\"Забронировать билеты\", \"Подготовить документы\", \"Упаковать чемодан\"]")
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Шаблон с указанным ID не найден"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @GetMapping("/{templateId}/items")
    public ResponseEntity<List<String>> getTemplateItems(
            @Parameter(description = "Уникальный идентификатор шаблона", required = true, example = "1")
            @PathVariable Long templateId) {
        return todoTemplateRepository.findById(templateId)
                .map(template -> {
                    List<TemplateItem> items = templateItemRepository.findByTemplateOrderByOrderPosition(template);
                    List<String> contents = items.stream()
                            .map(TemplateItem::getContent)
                            .collect(Collectors.toList());
                    return ResponseEntity.ok(contents);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Получить шаблоны по категории",
            description = "Возвращает список шаблонов задач для конкретной категории поездки. Доступные категории: 'business' (деловые), 'vacation' (отпуск), 'adventure' (приключения), 'family' (семейные), 'study' (учебные)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Шаблоны категории успешно получены",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TodoTemplateDto.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Некорректная категория. Разрешены: business, vacation, adventure, family, study"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @GetMapping("/category/{category}")
    public ResponseEntity<List<TodoTemplateDto>> getTemplatesByCategory(
            @Parameter(
                    description = "Категория шаблонов",
                    required = true,
                    example = "business",
                    schema = @Schema(
                            type = "string",
                            allowableValues = {"business", "vacation", "adventure", "family", "study"}
                    )
            )
            @PathVariable
            @Pattern(regexp = "^(business|vacation|adventure|family|study)$", message = "Категория должна быть одной из: business, vacation, adventure, family, study")
            String category) {
        List<TodoTemplate> templates = todoTemplateRepository.findByCategory(category);

        List<TodoTemplateDto> result = templates.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    private TodoTemplateDto convertToDto(TodoTemplate template) {
        TodoTemplateDto dto = new TodoTemplateDto();
        dto.setTemplateId(template.getTemplateId());
        dto.setTitle(template.getTitle());
        dto.setDescription(template.getDescription());
        dto.setCategory(template.getCategory());
        dto.setIsSystem(template.getIsSystem());

        if (template.getCreatedBy() != null) {
            dto.setCreatedBy(template.getCreatedBy().getUserId());
        }

        dto.setCreatedAt(template.getCreatedAt());
        dto.setUpdatedAt(template.getUpdatedAt());

        List<TemplateItemDto> itemDtos = template.getItems().stream()
                .map(item -> {
                    TemplateItemDto itemDto = new TemplateItemDto();
                    itemDto.setItemId(item.getItemId());
                    itemDto.setTemplateId(template.getTemplateId());
                    itemDto.setContent(item.getContent());
                    itemDto.setOrderPosition(item.getOrderPosition());
                    itemDto.setCreatedAt(item.getCreatedAt());
                    return itemDto;
                })
                .collect(Collectors.toList());

        dto.setItems(itemDtos);
        return dto;
    }
} 