package ru.putevod.app.planner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateItemDto {

    private Long itemId;
    private Long templateId;
    private String content;
    private Integer orderPosition;
    private LocalDateTime createdAt;
} 