package ru.putevod.app.external.dto.response;

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
public class YandexGeoSuggestResponse {

    @JsonProperty("suggest_reqid")
    private String requestId;

    @JsonProperty("results")
    private List<GeoSuggestItem> results;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeoSuggestItem {

        @JsonProperty("title")
        private TextWithHighlight title;

        @JsonProperty("subtitle")
        private TextWithHighlight subtitle;

        @JsonProperty("tags")
        private List<String> tags;

        @JsonProperty("distance")
        private Distance distance;

        @JsonProperty("geometry")
        private Geometry geometry;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TextWithHighlight {

        @JsonProperty("text")
        private String text;

        @JsonProperty("hl")
        private List<Highlight> highlights;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Highlight {

        @JsonProperty("begin")
        private Integer begin;

        @JsonProperty("end")
        private Integer end;

        @JsonProperty("type")
        private String type;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Distance {

        @JsonProperty("value")
        private Double value;

        @JsonProperty("text")
        private String text;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Geometry {

        @JsonProperty("location")
        private Location location;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Location {

        @JsonProperty("lat")
        private Double lat;

        @JsonProperty("lon")
        private Double lon;
    }
} 