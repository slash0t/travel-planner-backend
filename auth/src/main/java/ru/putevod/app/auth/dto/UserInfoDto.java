package ru.putevod.app.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Информация о пользователе")
public class UserInfoDto {

    @Schema(description = "Уникальный идентификатор пользователя", example = "123")
    private Integer id;
    
    @Schema(description = "Email пользователя", example = "user@example.com")
    private String email;
    
    @Schema(description = "Имя пользователя", example = "John Doe")
    private String username;
    
    @Schema(description = "URL аватара пользователя", example = "https://example.com/avatars/user123.jpg")
    private String avatarUrl;
    
    @Schema(description = "Флаг подтверждения email", example = "true")
    private boolean emailVerified;
    
    @Schema(description = "Дата и время регистрации", example = "2023-10-15T12:30:45")
    private LocalDateTime createdAt;
} 