package ru.putevod.app.planner.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Место")
public class PlaceDto {
    @Schema(description = "Идентификатор места", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;
    
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
    
    @Schema(description = "Дата создания", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;
    
    @Schema(description = "Дата последнего обновления", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;

    @Builder.Default
    @Schema(description = "Фотографии места", accessMode = Schema.AccessMode.READ_ONLY)
    private List<PhotoDto> photos = new ArrayList<>();
} 