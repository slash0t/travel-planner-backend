package ru.putevod.app.planner.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Информация о доступе к поездке")
public class TripAccessDto {
    @Schema(description = "Идентификатор доступа", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;
    
    @Schema(description = "Идентификатор поездки", accessMode = Schema.AccessMode.READ_ONLY)
    private Long tripId;
    
    @Schema(description = "Информация о пользователе с доступом", accessMode = Schema.AccessMode.READ_ONLY)
    private UserDto user;
    
    @NotBlank(message = "Уровень доступа обязателен для заполнения")
    @Schema(description = "Уровень доступа", allowableValues = {"read", "write", "admin"}, example = "read", requiredMode = Schema.RequiredMode.REQUIRED)
    private String accessLevel;
    
    @Schema(description = "Статус приглашения", allowableValues = {"pending", "accepted", "rejected"}, example = "pending", accessMode = Schema.AccessMode.READ_ONLY)
    private String invitationStatus;
    
    @Schema(description = "Дата создания доступа", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;
    
    @Schema(description = "Дата последнего обновления", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;
} 