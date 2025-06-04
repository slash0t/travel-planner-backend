package ru.putevod.app.library.dto.planner;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripDetailDto {
    private Long tripId;
    private String title;
    private String description;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    
    private String country;
    private String city;
    private boolean published;
    private String previewUrl;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    private List<TripDayDto> days;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TripDayDto {
        private Long dayId;
        private Integer dayNumber;
        
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate date;
        
        private String description;
        private List<EventDto> events;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventDto {
        private Long eventId;
        private String title;
        private String description;
        private String startTime;
        private String endTime;
        private PlaceDto place;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlaceDto {
        private Long placeId;
        private String name;
        private String address;
        private Double latitude;
        private Double longitude;
        private String externalId;
    }
} 