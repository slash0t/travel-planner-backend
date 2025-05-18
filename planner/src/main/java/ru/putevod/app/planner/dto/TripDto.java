package ru.putevod.app.planner.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TripDto {
    private Long id;
    private UserDto creator;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String country;
    private String city;
    private boolean published;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Builder.Default
    private List<TripDayDto> days = new ArrayList<>();
    
    @Builder.Default
    private List<TripAccessDto> access = new ArrayList<>();
    
    @Builder.Default
    private List<FileDto> files = new ArrayList<>();
    
    @Builder.Default
    private List<TodoListDto> todoLists = new ArrayList<>();
    
    private String status;
    private int totalDays;
} 