package ru.putevod.app.planner.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Информация о поездке")
public class TripDto {
    @Schema(description = "Идентификатор поездки", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;
    
    @Schema(description = "Информация о создателе поездки", accessMode = Schema.AccessMode.READ_ONLY)
    private UserDto creator;
    
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
    
    @Schema(description = "URL превью изображения поездки", example = "https://example.com/image.jpg")
    private String previewUrl;
    
    @Schema(description = "Дата создания", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;
    
    @Schema(description = "Дата последнего обновления", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;
    
    @Builder.Default
    @Schema(description = "Список дней поездки", accessMode = Schema.AccessMode.READ_ONLY)
    private List<TripDayDto> days = new ArrayList<>();
    
    @Builder.Default
    @Schema(description = "Список доступов к поездке", accessMode = Schema.AccessMode.READ_ONLY)
    private List<TripAccessDto> access = new ArrayList<>();
    
    @Builder.Default
    @Schema(description = "Список файлов поездки", accessMode = Schema.AccessMode.READ_ONLY)
    private List<FileDto> files = new ArrayList<>();
    
    @Builder.Default
    @Schema(description = "Списки задач поездки", accessMode = Schema.AccessMode.READ_ONLY)
    private List<TodoListDto> todoLists = new ArrayList<>();
    
    @Schema(description = "Статус поездки (upcoming, ongoing, past)", accessMode = Schema.AccessMode.READ_ONLY)
    private String status;
    
    @Schema(description = "Общее количество дней поездки", accessMode = Schema.AccessMode.READ_ONLY)
    private int totalDays;
} 