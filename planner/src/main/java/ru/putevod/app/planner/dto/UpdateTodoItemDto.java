package ru.putevod.app.planner.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "DTO для обновления элемента списка задач")
public class UpdateTodoItemDto {

    @Size(max = 500, message = "Содержимое задачи не может превышать 500 символов")
    @Schema(description = "Содержимое задачи", example = "Купить билеты на самолет", maxLength = 500)
    private String content;

    @Schema(description = "Статус выполнения задачи", example = "true")
    private Boolean completed;

    @Positive(message = "Позиция должна быть положительным числом")
    @Schema(description = "Позиция в списке (для сортировки)", example = "2")
    private Integer orderPosition;
} 