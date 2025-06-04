package ru.putevod.app.planner.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UnsplashResponseDto {

    private Integer total;

    @JsonProperty("total_pages")
    private Integer totalPages;

    private List<UnsplashImage> results;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UnsplashImage {
        private String id;
        private String description;

        @JsonProperty("alt_description")
        private String altDescription;

        private Urls urls;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Urls {
            private String raw;
            private String full;
            private String regular;
            private String small;
            private String thumb;
        }
    }
} 