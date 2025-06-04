package ru.putevod.app.planner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ответ при удалении доступа к поездке")
public class RemoveShareResponseDto {
    @Schema(description = "Сообщение о результате операции", example = "Доступ к поездке успешно удален")
    private String message;
    
    @Schema(description = "ID пользователя, у которого был удален доступ", example = "123")
    private Long removedUserId;
    
    @Schema(description = "Никнейм пользователя, у которого был удален доступ", example = "john_doe")
    private String removedUsername;
    
    @Schema(description = "Статус удаленного приглашения", allowableValues = {"pending", "accepted", "rejected"}, example = "pending")
    private String previousInvitationStatus;
    
    @Schema(description = "Уровень доступа, который был удален", allowableValues = {"read", "write", "admin"}, example = "read")
    private String previousAccessLevel;
} 