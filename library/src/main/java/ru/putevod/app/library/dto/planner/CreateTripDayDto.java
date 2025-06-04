package ru.putevod.app.library.dto.planner;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTripDayDto {
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    
    private String note;
} 