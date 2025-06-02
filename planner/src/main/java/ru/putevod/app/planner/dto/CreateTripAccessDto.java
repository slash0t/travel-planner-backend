package ru.putevod.app.planner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Данные для предоставления доступа к поездке")
public class CreateTripAccessDto {
    @NotNull(message = "ID пользователя обязателен для заполнения")
    @Schema(description = "Идентификатор пользователя, которому предоставляется доступ", example = "123", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;
    
    @NotBlank(message = "Уровень доступа обязателен для заполнения")
    @Pattern(regexp = "VIEW|EDIT|ADMIN", message = "Уровень доступа должен быть одним из: VIEW, EDIT, ADMIN")
    @Schema(description = "Уровень доступа", allowableValues = {"VIEW", "EDIT", "ADMIN"}, example = "VIEW", requiredMode = Schema.RequiredMode.REQUIRED)
    private String accessLevel;
} 