package ru.putevod.app.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationRequest {

    @NotBlank(message = "Токен подтверждения не может быть пустым")
    private String token;
} 