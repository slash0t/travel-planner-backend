package ru.putevod.app.external.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackingListTemplate {
    private String id;
    private String name;
    private String description;
    private String category;
    private String imageUrl;
    private Integer itemCount;
} 