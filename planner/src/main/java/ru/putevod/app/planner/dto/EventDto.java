package ru.putevod.app.planner.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventDto {
    private Long id;
    private Long dayId;
    private PlaceDto place;
    private String title;
    private String description;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean hasSpecificTime;
    private String notes;
    private Integer orderPosition;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Builder.Default
    private List<FileDto> files = new ArrayList<>();
    
    @Builder.Default
    private List<EventReminderDto> reminders = new ArrayList<>();
} 