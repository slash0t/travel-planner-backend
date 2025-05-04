package ru.putevod.app.planner.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
public class NotificationDto {
    private Long id;
    private Long userId;
    private String type;
    private String content;
    private Integer relatedId;
    private boolean read;
    private LocalDateTime createdAt;
} 