package ru.putevod.app.planner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
    @Schema(description = "Идентификатор пользователя, которому предоставляется доступ (если известен)", example = "123")
    private Long userId;

    @Schema(description = "Никнейм пользователя, которому предоставляется доступ", example = "john_doe")
    private String username;

    @NotBlank(message = "Уровень доступа обязателен для заполнения")
    @Pattern(regexp = "read|write|admin", message = "Уровень доступа должен быть одним из: read, write, admin")
    @Schema(description = "Уровень доступа", allowableValues = {"read", "write", "admin"}, example = "read", requiredMode = Schema.RequiredMode.REQUIRED)
    private String accessLevel;
} 