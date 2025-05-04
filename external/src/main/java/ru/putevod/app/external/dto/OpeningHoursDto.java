package ru.putevod.app.external.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpeningHoursDto {
    private String day;
    private String open;
    private String close;
    private Boolean closed;
} 