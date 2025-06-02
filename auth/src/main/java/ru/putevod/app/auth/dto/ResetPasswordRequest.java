package ru.putevod.app.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {

    @NotBlank(message = "Токен сброса пароля не может быть пустым")
    private String resetToken;

    @NotBlank(message = "Новый пароль не может быть пустым")
    private String newPassword;
} 