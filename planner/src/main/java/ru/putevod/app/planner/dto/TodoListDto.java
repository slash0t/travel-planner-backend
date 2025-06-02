package ru.putevod.app.planner.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Список задач")
public class TodoListDto {
    @Schema(description = "Идентификатор списка задач", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;
    
    @Schema(description = "Идентификатор владельца списка", accessMode = Schema.AccessMode.READ_ONLY)
    private Long userId;
    
    @Schema(description = "Идентификатор поездки (если привязан к поездке)", accessMode = Schema.AccessMode.READ_ONLY)
    private Long tripId;
    
    @NotBlank(message = "Название списка задач обязательно для заполнения")
    @Size(min = 3, max = 100, message = "Название списка должно содержать от 3 до 100 символов")
    @Schema(description = "Название списка задач", example = "Подготовка к поездке", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;
    
    @Size(max = 500, message = "Описание списка не должно превышать 500 символов")
    @Schema(description = "Описание списка задач", example = "Список дел перед отъездом")
    private String description;
    
    @Schema(description = "Тип списка", allowableValues = {"PERSONAL", "TRIP", "TEMPLATE"}, example = "PERSONAL")
    private String listType;
    
    @Schema(description = "Дата создания", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;
    
    @Schema(description = "Дата последнего обновления", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;

    @Builder.Default
    @Schema(description = "Элементы списка задач", accessMode = Schema.AccessMode.READ_ONLY)
    private List<TodoItemDto> items = new ArrayList<>();

    @Schema(description = "Общее количество задач", accessMode = Schema.AccessMode.READ_ONLY)
    private int itemCount;
    
    @Schema(description = "Количество выполненных задач", accessMode = Schema.AccessMode.READ_ONLY)
    private int completedCount;
} 