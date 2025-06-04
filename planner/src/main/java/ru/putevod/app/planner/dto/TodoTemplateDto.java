package ru.putevod.app.planner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO шаблона задач с полной информацией и элементами")
public class TodoTemplateDto {

    @Schema(description = "Уникальный идентификатор шаблона", example = "1")
    private Long templateId;

    @Schema(description = "Название шаблона", example = "Деловая поездка")
    private String title;

    @Schema(description = "Описание шаблона", example = "Стандартный набор задач для деловой поездки")
    private String description;

    @Schema(description = "Категория шаблона", example = "business", allowableValues = {"business", "vacation", "adventure", "family", "study"})
    private String category;

    @Schema(description = "Является ли шаблон системным (создан администратором)", example = "true")
    private Boolean isSystem;

    @Schema(description = "ID пользователя создавшего шаблон", example = "1")
    private Long createdBy;

    @Schema(description = "Список элементов (задач) шаблона")
    private List<TemplateItemDto> items = new ArrayList<>();

    @Schema(description = "Дата и время создания шаблона", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Дата и время последнего обновления шаблона", example = "2024-01-15T10:30:00")
    private LocalDateTime updatedAt;
}