package ru.putevod.app.planner.controller;

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
public class TodoTemplateController {

    private final TodoTemplateRepository todoTemplateRepository;
    private final TemplateItemRepository templateItemRepository;
    
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
    
    @GetMapping("/{templateId}")
    public ResponseEntity<Map<String, Object>> getTemplateDetails(@PathVariable Long templateId) {
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
    
    @GetMapping("/{templateId}/items")
    public ResponseEntity<List<String>> getTemplateItems(@PathVariable Long templateId) {
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
    
    @GetMapping("/category/{category}")
    public ResponseEntity<List<TodoTemplateDto>> getTemplatesByCategory(@PathVariable String category) {
        List<TodoTemplate> templates = todoTemplateRepository.findByCategory(category);
        if (templates.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        
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