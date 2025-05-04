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
public class PackingListTemplatesResponse {
    private List<PackingListTemplate> templates;
} 