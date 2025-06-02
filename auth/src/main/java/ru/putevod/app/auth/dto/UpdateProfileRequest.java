package ru.putevod.app.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на обновление профиля пользователя")
public class UpdateProfileRequest {

    @Schema(description = "Новое имя пользователя", example = "newusername")
    @Size(min = 3, max = 50, message = "Имя пользователя должно содержать от 3 до 50 символов")
    private String username;

    @Schema(description = "Новый email пользователя", example = "newemail@example.com")
    @Email(message = "Некорректный формат email")
    @Size(max = 100, message = "Email не должен превышать 100 символов")
    private String email;

    @Schema(description = "URL нового аватара пользователя", example = "https://example.com/avatars/new-avatar.jpg")
    @Size(max = 500, message = "URL аватара не должен превышать 500 символов")
    private String avatarUrl;
} 