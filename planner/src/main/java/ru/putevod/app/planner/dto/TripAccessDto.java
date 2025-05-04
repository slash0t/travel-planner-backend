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
public class TripAccessDto {
    private Long id;
    private Long tripId;
    private UserDto user;
    private String accessLevel;
    private String invitationStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 