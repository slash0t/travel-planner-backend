package ru.putevod.app.planner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
@Schema(description = "Данные для обновления поездки")
public class UpdateTripDto {
    @NotBlank(message = "Название поездки обязательно для заполнения")
    @Size(min = 3, max = 100, message = "Название поездки должно содержать от 3 до 100 символов")
    @Schema(description = "Название поездки", example = "Поездка в Санкт-Петербург", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Size(max = 1000, message = "Описание поездки не должно превышать 1000 символов")
    @Schema(description = "Описание поездки", example = "Культурная столица России")
    private String description;

    @NotNull(message = "Дата начала поездки обязательна для заполнения")
    @Schema(description = "Дата начала поездки", example = "2024-07-15", requiredMode = Schema.RequiredMode.REQUIRED, type = "string", format = "date")
    private LocalDate startDate;

    @NotNull(message = "Дата окончания поездки обязательна для заполнения")
    @Schema(description = "Дата окончания поездки", example = "2024-07-20", requiredMode = Schema.RequiredMode.REQUIRED, type = "string", format = "date")
    private LocalDate endDate;

    @NotBlank(message = "Страна поездки обязательна для заполнения")
    @Size(min = 2, max = 50, message = "Название страны должно содержать от 2 до 50 символов")
    @Schema(description = "Страна поездки", example = "Россия", requiredMode = Schema.RequiredMode.REQUIRED)
    private String country;

    @NotBlank(message = "Город поездки обязателен для заполнения")
    @Size(min = 2, max = 50, message = "Название города должно содержать от 2 до 50 символов")
    @Schema(description = "Город поездки", example = "Санкт-Петербург", requiredMode = Schema.RequiredMode.REQUIRED)
    private String city;

    @Schema(description = "Флаг публикации поездки", defaultValue = "false")
    private boolean published;
} 