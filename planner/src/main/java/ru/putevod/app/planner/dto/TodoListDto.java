package ru.putevod.app.planner.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TodoListDto {
    private Long id;
    private Long userId;
    private Long tripId;
    private String title;
    private String description;
    private String listType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Builder.Default
    private List<TodoItemDto> items = new ArrayList<>();
    
    private int itemCount;
    private int completedCount;
} 