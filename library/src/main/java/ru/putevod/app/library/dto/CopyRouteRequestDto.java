package ru.putevod.app.library.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO для копирования опубликованного маршрута")
public class CopyRouteRequestDto {

    @NotNull(message = "Дата начала путешествия обязательна")
    @FutureOrPresent(message = "Дата начала не может быть в прошлом")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Дата начала путешествия", example = "2024-06-15", required = true)
    private LocalDate startDate;

    @Schema(description = "Название для нового маршрута (если не указано, используется оригинальное)")
    private String title;
} 