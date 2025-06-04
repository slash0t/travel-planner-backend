package ru.putevod.app.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationWithMigrationRequest {

    @NotBlank(message = "Токен подтверждения не может быть пустым")
    private String token;

    /**
     * ID устройства для миграции данных анонимного пользователя (опционально)
     */
    private String deviceId;
} 