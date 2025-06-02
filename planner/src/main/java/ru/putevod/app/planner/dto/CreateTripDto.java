package ru.putevod.app.planner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Данные для создания поездки")
public class CreateTripDto {
    @Schema(description = "Название поездки", example = "Поездка в Санкт-Петербург", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(description = "Описание поездки", example = "Культурная столица России")
    private String description;

    @Schema(description = "Дата начала поездки", example = "2024-07-15", requiredMode = Schema.RequiredMode.REQUIRED, type = "string", format = "date")
    private LocalDate startDate;

    @Schema(description = "Дата окончания поездки", example = "2024-07-20", requiredMode = Schema.RequiredMode.REQUIRED, type = "string", format = "date")
    private LocalDate endDate;

    @Schema(description = "Страна поездки", example = "Россия", requiredMode = Schema.RequiredMode.REQUIRED)
    private String country;

    @Schema(description = "Город поездки", example = "Санкт-Петербург", requiredMode = Schema.RequiredMode.REQUIRED)
    private String city;

    @Schema(description = "Флаг публикации поездки", defaultValue = "false")
    private boolean published;

    @Schema(description = "URL превью изображения поездки (генерируется автоматически)", example = "https://example.com/image.jpg", accessMode = Schema.AccessMode.READ_ONLY)
    private String previewUrl;
} 