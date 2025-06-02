package ru.putevod.app.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ответ с данными авторизации пользователя")
public class AuthResponse {

    @Schema(description = "JWT токен доступа", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "Токен для обновления JWT токена доступа", example = "5a64d0d1-8d3a-4c3f-bc3e-95c5f45...")
    private String refreshToken;

    @Schema(description = "Срок действия токена в секундах", example = "3600")
    private int expiresIn;

    @Schema(description = "Информация о пользователе")
    private UserInfoDto user;
} 