package ru.putevod.app.library.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PublicRouteDetailDto extends PublicRouteDto {
    private List<DayDto> days;

    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DayDto {
        private Integer day;
        private List<PlaceDto> places;
    }

    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlaceDto {
        private Long id;
        private String name;
        private String description;
        private String category;
        private LocationDto location;
        private String startTime;
        private String endTime;
        private String imageUrl;
    }

    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationDto {
        private Double lat;
        private Double lng;
    }
} 