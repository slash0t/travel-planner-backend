package ru.putevod.app.library.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
@Schema(description = "Ответ при копировании маршрута")
public class CopyRouteResponseDto {

    @Schema(description = "ID созданного маршрута", example = "123")
    private Long tripId;

    @Schema(description = "Название скопированного маршрута", example = "Путешествие по Москве")
    private String title;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Дата начала путешествия", example = "2024-06-15")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd") 
    @Schema(description = "Дата окончания путешествия", example = "2024-06-20")
    private LocalDate endDate;

    @Schema(description = "Длительность путешествия в днях", example = "6")
    private Integer duration;

    @Schema(description = "Количество скопированных дней маршрута", example = "5")
    private Integer copiedDaysCount;
} 