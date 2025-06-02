package ru.putevod.app.planner.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.putevod.app.planner.dto.TemplateItemDto;
import ru.putevod.app.planner.dto.TodoTemplateDto;
import ru.putevod.app.planner.model.TemplateItem;
import ru.putevod.app.planner.model.TodoTemplate;
import ru.putevod.app.planner.repository.TemplateItemRepository;
import ru.putevod.app.planner.repository.TodoTemplateRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        description = "Возвращает список всех доступных шаблонов задач. Шаблоны содержат готовые наборы задач для различных типов поездок."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Список шаблонов успешно получен"),
        @ApiResponse(responseCode = "204", description = "Шаблоны не найдены"),
        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @GetMapping
    public ResponseEntity<List<TodoTemplateDto>> getAllTemplates() {
        List<TodoTemplate> templates = todoTemplateRepository.findAll();
        if (templates.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        
        List<TodoTemplateDto> result = templates.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(result);
    }
    
    @Operation(
        summary = "Получить детали шаблона",
        description = "Возвращает подробную информацию о конкретном шаблоне задач включая его метаданные."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Детали шаблона успешно получены"),
        @ApiResponse(responseCode = "404", description = "Шаблон не найден"),
        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @GetMapping("/{templateId}")
    public ResponseEntity<Map<String, Object>> getTemplateDetails(
            @Parameter(description = "Уникальный идентификатор шаблона", required = true, example = "1")
            @PathVariable Long templateId) {
        return todoTemplateRepository.findById(templateId)
                .map(template -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("templateId", template.getTemplateId());
                    result.put("title", template.getTitle());
                    result.put("description", template.getDescription());
                    result.put("category", template.getCategory());
                    result.put("isSystem", template.getIsSystem());
                    return ResponseEntity.ok(result);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @Operation(
        summary = "Получить элементы шаблона",
        description = "Возвращает список всех задач (элементов) конкретного шаблона в правильном порядке."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Элементы шаблона успешно получены"),
        @ApiResponse(responseCode = "404", description = "Шаблон не найден"),
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
        description = "Возвращает список шаблонов задач для конкретной категории поездки (например, 'business', 'vacation', 'adventure')."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Шаблоны категории успешно получены"),
        @ApiResponse(responseCode = "204", description = "Шаблоны для данной категории не найдены"),
        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @GetMapping("/category/{category}")
    public ResponseEntity<List<TodoTemplateDto>> getTemplatesByCategory(
            @Parameter(description = "Категория шаблонов", required = true, example = "business")
            @PathVariable String category) {
        List<TodoTemplate> templates = todoTemplateRepository.findByCategory(category);
        if (templates.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        
        List<TodoTemplateDto> result = templates.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(result);
    }
    
    TodoTemplateDto convertToDto(TodoTemplate template) {
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