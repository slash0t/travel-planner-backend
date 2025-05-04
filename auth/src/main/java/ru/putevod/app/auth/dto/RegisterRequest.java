package ru.putevod.app.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на регистрацию нового пользователя")
public class RegisterRequest {

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный формат email")
    @Schema(description = "Email пользователя", example = "user@example.com", required = true)
    private String email;

    @NotBlank(message = "Пароль не может быть пустым")
    @Schema(description = "Пароль пользователя", example = "password123", required = true)
    private String password;
    
    @NotBlank(message = "Имя пользователя не может быть пустым")
    @Schema(description = "Имя пользователя (никнейм)", example = "johndoe", required = true)
    private String username;
    
    @Schema(description = "Имя пользователя", example = "John")
    private String firstName;
    
    @Schema(description = "Фамилия пользователя", example = "Doe")
    private String lastName;
    
    @Schema(description = "Идентификатор устройства для push-уведомлений", example = "fcm-token-123")
    private String deviceId;
} 