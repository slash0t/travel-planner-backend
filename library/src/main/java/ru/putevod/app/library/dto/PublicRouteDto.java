package ru.putevod.app.library.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PublicRouteDto {
    private Long id;
    private Long originalRouteId;
    private String title;
    private String description;
    private RoutePreviewDto.AuthorDto author;
    private List<String> countries;
    private List<String> cities;
    private Integer duration;
    private Double rating;
    private Integer reviewsCount;
    private String previewImageUrl;
    private List<String> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 