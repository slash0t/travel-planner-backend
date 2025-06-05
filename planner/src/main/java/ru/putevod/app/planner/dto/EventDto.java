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
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Событие в поездке")
public class EventDto {
    @Schema(description = "Идентификатор события", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Идентификатор дня поездки", accessMode = Schema.AccessMode.READ_ONLY)
    private Long dayId;

    @Schema(description = "Информация о месте события", accessMode = Schema.AccessMode.READ_ONLY)
    private PlaceDto place;

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

    @Schema(description = "Дата создания", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "Дата последнего обновления", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;

    @Builder.Default
    @Schema(description = "Файлы события", accessMode = Schema.AccessMode.READ_ONLY)
    private List<FileDto> files = new ArrayList<>();

    @Builder.Default
    @Schema(description = "Напоминания о событии", accessMode = Schema.AccessMode.READ_ONLY)
    private List<EventReminderDto> reminders = new ArrayList<>();
} 