package ru.putevod.app.planner.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
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
@Schema(description = "Данные для перемещения события без времени")
public class ReorderEventDto {
    @NotNull(message = "Новая позиция обязательна для заполнения")
    @Positive(message = "Позиция должна быть положительным числом")
    @Schema(description = "Новая позиция события в списке", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer newPosition;
} 