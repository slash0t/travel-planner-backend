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
public class TodoItemDto {
    private Long id;
    private Long listId;
    private String content;
    private boolean completed;
    private Integer orderPosition;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 