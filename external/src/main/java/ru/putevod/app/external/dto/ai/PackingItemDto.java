package ru.putevod.app.external.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackingItemDto {
    private String name;
    private String description;
    private String priority;
    private Integer quantity;
} 