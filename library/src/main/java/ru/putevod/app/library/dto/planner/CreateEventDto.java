package ru.putevod.app.library.dto.planner;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateEventDto {
    private String title;
    private String description;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean hasSpecificTime = false;
    private String notes;
    private Integer orderPosition;
    private PlaceInfo place;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlaceInfo {
        private String name;
        private BigDecimal latitude;
        private BigDecimal longitude;
        private String address;
        private String placeType;
        private String externalId;
        private String previewUrl;
    }
} 