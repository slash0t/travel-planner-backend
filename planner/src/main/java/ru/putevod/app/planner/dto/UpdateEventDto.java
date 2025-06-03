package ru.putevod.app.planner.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Данные для обновления события")
public class UpdateEventDto {
    @NotBlank(message = "Название события обязательно для заполнения")
    @Size(min = 3, max = 100, message = "Название события должно содержать от 3 до 100 символов")
    @Schema(description = "Название события", example = "Посещение Эрмитажа", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;
    
    @Size(max = 1000, message = "Описание события не должно превышать 1000 символов")
    @Schema(description = "Описание события", example = "Экскурсия по главному музею города")
    private String description;
    
    @Schema(description = "Время начала события", example = "10:00", type = "string", format = "time")
    private LocalTime startTime;
    
    @Schema(description = "Время окончания события", example = "12:00", type = "string", format = "time")
    private LocalTime endTime;
    
    @Schema(description = "Флаг наличия конкретного времени", defaultValue = "false")
    private boolean hasSpecificTime;
    
    @Size(max = 1000, message = "Заметки не должны превышать 1000 символов")
    @Schema(description = "Дополнительные заметки", example = "Взять паспорт для льготного билета")
    private String notes;
    
    @Schema(description = "Позиция в порядке событий дня", example = "1")
    private Integer orderPosition;

    @Valid
    @Schema(description = "Информация о месте события (если null - место будет удалено)")
    private PlaceInfo place;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Информация о месте")
    public static class PlaceInfo {
        @NotBlank(message = "Название места обязательно для заполнения")
        @Size(min = 2, max = 100, message = "Название места должно содержать от 2 до 100 символов")
        @Schema(description = "Название места", example = "Государственный Эрмитаж", requiredMode = Schema.RequiredMode.REQUIRED)
        private String name;
        
        @Schema(description = "Широта", example = "59.939866")
        private BigDecimal latitude;
        
        @Schema(description = "Долгота", example = "30.314496")
        private BigDecimal longitude;
        
        @Size(max = 255, message = "Адрес не должен превышать 255 символов")
        @Schema(description = "Адрес места", example = "Дворцовая пл., 2, Санкт-Петербург")
        private String address;
        
        @Schema(description = "Тип места", example = "museum")
        private String placeType;
        
        @Schema(description = "Внешний идентификатор места", example = "google_places_123")
        private String externalId;
        
        @Schema(description = "URL изображения места", example = "https://example.com/hermitage.jpg")
        private String previewUrl;
    }
} 