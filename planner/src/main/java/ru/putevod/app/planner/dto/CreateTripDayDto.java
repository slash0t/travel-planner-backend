package ru.putevod.app.planner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Данные для создания дня поездки")
public class CreateTripDayDto {
    @NotNull(message = "Дата дня поездки обязательна для заполнения")
    @Schema(description = "Дата дня поездки", example = "2024-07-15", requiredMode = Schema.RequiredMode.REQUIRED, type = "string", format = "date")
    private LocalDate date;
    
    @Size(max = 1000, message = "Заметка дня не должна превышать 1000 символов")
    @Schema(description = "Заметка для дня поездки", example = "Первый день - осмотр центра города")
    private String note;
} 