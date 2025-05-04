package ru.putevod.app.library.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RoutePreviewDto {
    private UUID id;
    private String title;
    private String description;
    private AuthorDto author;
    private List<String> countries;
    private List<String> cities;
    private Integer duration;
    private Double rating;
    private Integer reviewsCount;
    private String previewImageUrl;
    private List<String> tags;
    private LocalDateTime createdAt;

    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorDto {
        private UUID id;
        private String username;
        private String avatarUrl;
    }
} 