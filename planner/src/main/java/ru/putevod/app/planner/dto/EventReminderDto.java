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
public class EventReminderDto {
    private Long id;
    private Long eventId;
    private Long userId;
    private LocalDateTime remindAt;
    private Integer minutesBefore;
    private boolean sent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 