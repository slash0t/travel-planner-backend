package ru.putevod.app.external.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoDto {
    private String url;
    private Integer width;
    private Integer height;
} 