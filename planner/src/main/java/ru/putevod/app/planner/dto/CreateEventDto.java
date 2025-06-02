package ru.putevod.app.planner.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * DTO для создания события.
 * Если событие привязано к месту (имеет координаты), то заполняются поля place.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateEventDto {
    private String title;
    private String description;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean hasSpecificTime;
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