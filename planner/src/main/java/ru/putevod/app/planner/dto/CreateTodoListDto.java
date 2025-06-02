package ru.putevod.app.planner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Данные для создания списка задач")
public class CreateTodoListDto {
    @NotBlank(message = "Название списка задач обязательно для заполнения")
    @Size(min = 3, max = 100, message = "Название списка должно содержать от 3 до 100 символов")
    @Schema(description = "Название списка задач", example = "Подготовка к поездке", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;
    
    @Size(max = 500, message = "Описание списка не должно превышать 500 символов")
    @Schema(description = "Описание списка задач", example = "Список дел перед отъездом")
    private String description;
    
    @Pattern(regexp = "PERSONAL|TRIP|TEMPLATE", message = "Тип списка должен быть одним из: PERSONAL, TRIP, TEMPLATE")
    @Schema(description = "Тип списка", allowableValues = {"PERSONAL", "TRIP", "TEMPLATE"}, example = "PERSONAL", defaultValue = "PERSONAL")
    private String listType = "PERSONAL";
} 