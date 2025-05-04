package ru.putevod.app.external.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceResponseDto {
    private String id;
    private String name;
    private String description;
    private String address;
    private String previewUrl;
    private Double lat;
    private Double lon;
    private String category;
    private Float rating;
    private Integer distanceMeters;
    private String phone;
    private String website;
    private List<OpeningHoursDto> openingHours;
    private List<PhotoDto> photos;
    private String sourceSystem;
    private String externalId;
    private String externalUrl;
} 