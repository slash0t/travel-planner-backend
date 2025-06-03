package ru.putevod.app.planner.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "DTO для изменения порядка элемента списка задач")
public class ReorderTodoItemDto {

    @Positive(message = "Новая позиция должна быть положительным числом")
    @Schema(description = "Новая позиция элемента в списке", example = "3", minimum = "1")
    private Integer newPosition;
} 