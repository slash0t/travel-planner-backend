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
public class PixabayResponseDto {

    private Integer total;
    private Integer totalHits;
    private List<PixabayImage> hits;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PixabayImage {
        private Long id;
        private String pageURL;
        private String type;
        private String tags;

        @JsonProperty("previewURL")
        private String previewUrl;

        @JsonProperty("previewWidth")
        private Integer previewWidth;

        @JsonProperty("previewHeight")
        private Integer previewHeight;

        @JsonProperty("webformatURL")
        private String webformatUrl;

        @JsonProperty("webformatWidth")
        private Integer webformatWidth;

        @JsonProperty("webformatHeight")
        private Integer webformatHeight;

        @JsonProperty("largeImageURL")
        private String largeImageUrl;

        @JsonProperty("imageWidth")
        private Integer imageWidth;

        @JsonProperty("imageHeight")
        private Integer imageHeight;

        @JsonProperty("imageSize")
        private Integer imageSize;

        private Integer views;
        private Integer downloads;
        private Integer collections;
        private Integer likes;
        private Integer comments;

        @JsonProperty("user_id")
        private Long userId;

        private String user;

        @JsonProperty("userImageURL")
        private String userImageUrl;
    }
} 