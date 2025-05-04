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
public class PlaceRequestDto {
    private String destination;
    private List<String> interests;
    private Integer duration;
    private String travelStyle;
} 