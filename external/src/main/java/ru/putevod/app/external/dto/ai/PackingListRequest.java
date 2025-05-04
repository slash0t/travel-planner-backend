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
public class PackingListRequest {
    private String destination;
    private Integer duration;
    private String season;
    private String travelType;
    private List<ParticipantDto> participants;
    private List<String> activities;
    private List<String> transportation;
    private String accommodation;
    private String additionalInfo;
} 