package ru.putevod.app.planner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Данные для создания напоминания о событии")
public class CreateEventReminderDto {
    @Schema(description = "Дата и время напоминания", example = "2024-07-15T09:00:00")
    private LocalDateTime remindAt;
    
    @NotNull(message = "Количество минут до события обязательно для заполнения")
    @Positive(message = "Количество минут должно быть положительным числом")
    @Schema(description = "Количество минут до события для напоминания", example = "30", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer minutesBefore;
} 