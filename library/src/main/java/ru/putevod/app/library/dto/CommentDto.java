package ru.putevod.app.library.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
    private UUID id;
    private UUID routeId;
    private RoutePreviewDto.AuthorDto author;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 