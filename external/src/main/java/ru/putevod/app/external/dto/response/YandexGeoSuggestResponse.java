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
    
    @JsonProperty("items")
    private List<GeoSuggestItem> items;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeoSuggestItem {
        
        @JsonProperty("id")
        private String id;
        
        @JsonProperty("title")
        private String title;
        
        @JsonProperty("subtitle")
        private String subtitle;
        
        @JsonProperty("tags")
        private List<String> tags;
        
        @JsonProperty("type")
        private String type;
        
        @JsonProperty("distance")
        private Double distance;
        
        @JsonProperty("geometry")
        private Geometry geometry;
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