package ru.putevod.app.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ответ на запрос валидации токена")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenValidationResponse {
    @Schema(description = "Признак валидности токена", example = "true")
    private boolean valid;
    
    @Schema(description = "ID пользователя", example = "123")
    private Long userId;
    
    @Schema(description = "Email пользователя", example = "user@example.com")
    private String email;
    
    @Schema(description = "Имя пользователя", example = "johndoe")
    private String username;
    
    @Schema(description = "Признак, что пользователь является администратором", example = "false")
    private boolean admin;
    
    @Schema(description = "Сообщение об ошибке при невалидном токене", example = "Срок действия токена истек")
    private String errorMessage;
    
    @Schema(description = "Тип ошибки при невалидном токене", example = "ExpiredJwtException")
    private String errorType;
} 