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
public class PackingListResponse {
    private List<PackingCategoryDto> categories;
    private List<String> suggestions;
    private Integer totalItems;
} 