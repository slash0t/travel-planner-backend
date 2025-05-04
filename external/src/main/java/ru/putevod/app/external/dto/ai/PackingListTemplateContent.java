package ru.putevod.app.external.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackingListTemplateContent {
    private String id;
    private String name;
    private String description;
    private List<PackingCategoryDto> categories;
    private Integer totalItems;
} 