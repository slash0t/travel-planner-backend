package ru.putevod.app.planner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO элемента шаблона задач")
public class TemplateItemDto {

    @Schema(description = "Уникальный идентификатор элемента", example = "1")
    private Long itemId;

    @Schema(description = "ID шаблона к которому принадлежит элемент", example = "1")
    private Long templateId;

    @Schema(description = "Содержимое задачи", example = "Забронировать билеты")
    private String content;

    @Schema(description = "Позиция элемента в списке", example = "1")
    private Integer orderPosition;

    @Schema(description = "Дата и время создания элемента", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
} 