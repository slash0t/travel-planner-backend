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
public class YandexGeocoderResponse {

    @JsonProperty("response")
    private GeocoderResponse response;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeocoderResponse {

        @JsonProperty("GeoObjectCollection")
        private GeoObjectCollection geoObjectCollection;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeoObjectCollection {

        @JsonProperty("featureMember")
        private List<FeatureMember> featureMembers;

        @JsonProperty("metaDataProperty")
        private MetaDataProperty metaDataProperty;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FeatureMember {

        @JsonProperty("GeoObject")
        private GeoObject geoObject;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeoObject {

        @JsonProperty("Point")
        private Point point;

        @JsonProperty("name")
        private String name;

        @JsonProperty("description")
        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Point {

        @JsonProperty("pos")
        private String pos;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MetaDataProperty {

        @JsonProperty("GeocoderResponseMetaData")
        private GeocoderResponseMetaData geocoderResponseMetaData;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeocoderResponseMetaData {

        @JsonProperty("found")
        private String found;
    }
} 