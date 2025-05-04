package ru.putevod.app.external.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceSuggestionDto {
    private String id;
    private String name;
    private String address;
    private String category;
    private String previewUrl;
} 