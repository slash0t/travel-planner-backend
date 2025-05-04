package ru.putevod.app.external.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.putevod.app.external.dto.PlaceResponseDto;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceSearchResponse {
    private List<PlaceResponseDto> places;
    private Integer total;
} 