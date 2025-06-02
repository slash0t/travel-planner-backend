package ru.putevod.app.planner.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@Schema(description = "День поездки")
public class TripDayDto {
    @Schema(description = "Идентификатор дня поездки", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;
    
    @Schema(description = "Идентификатор поездки", accessMode = Schema.AccessMode.READ_ONLY)
    private Long tripId;
    
    @Schema(description = "Номер дня в поездке", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer dayNumber;
    
    @NotNull(message = "Дата дня поездки обязательна для заполнения")
    @Schema(description = "Дата дня поездки", example = "2024-07-15", requiredMode = Schema.RequiredMode.REQUIRED, type = "string", format = "date")
    private LocalDate date;
    
    @Size(max = 1000, message = "Заметка дня не должна превышать 1000 символов")
    @Schema(description = "Заметка для дня поездки", example = "Первый день - осмотр центра города")
    private String note;
    
    @Schema(description = "Дата создания", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;
    
    @Schema(description = "Дата последнего обновления", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;

    @Builder.Default
    @Schema(description = "События дня", accessMode = Schema.AccessMode.READ_ONLY)
    private List<EventDto> events = new ArrayList<>();
} 