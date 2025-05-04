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
public class PhotoDto {
    private Long id;
    private Long userId;
    private Long placeId;
    private Long eventId;
    private Long tripId;
    private FileDto file;
    private String caption;
    private LocalDateTime takenAt;
    private LocalDateTime createdAt;
    private String url;
} 