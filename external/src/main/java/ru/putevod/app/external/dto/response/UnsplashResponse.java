package ru.putevod.app.external.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO для ответа от Unsplash API
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UnsplashResponse {

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
        
        @JsonProperty("slug")
        private String slug;
        
        @JsonProperty("alternative_slugs")
        private AlternativeSlugs alternativeSlugs;
        
        @JsonProperty("created_at")
        private String createdAt;
        
        @JsonProperty("updated_at")
        private String updatedAt;
        
        @JsonProperty("promoted_at")
        private String promotedAt;
        
        private Integer width;
        private Integer height;
        private String color;
        
        @JsonProperty("blur_hash")
        private String blurHash;
        
        private String description;
        
        @JsonProperty("alt_description")
        private String altDescription;
        
        private List<String> breadcrumbs;
        
        private Urls urls;
        private Links links;
        private Integer likes;
        
        @JsonProperty("liked_by_user")
        private Boolean likedByUser;
        
        @JsonProperty("current_user_collections")
        private List<Object> currentUserCollections;
        
        private String sponsorship;
        
        @JsonProperty("topic_submissions")
        private Object topicSubmissions;
        
        @JsonProperty("asset_type")
        private String assetType;
        
        private User user;
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class AlternativeSlugs {
            private String en;
            private String es;
            private String ja;
            private String fr;
            private String it;
            private String ko;
            private String de;
            private String pt;
        }
        
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
            
            @JsonProperty("small_s3")
            private String smallS3;
        }
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Links {
            private String self;
            private String html;
            private String download;
            
            @JsonProperty("download_location")
            private String downloadLocation;
        }
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class User {
            private String id;
            
            @JsonProperty("updated_at")
            private String updatedAt;
            
            private String username;
            private String name;
            
            @JsonProperty("first_name")
            private String firstName;
            
            @JsonProperty("last_name")
            private String lastName;
            
            @JsonProperty("twitter_username")
            private String twitterUsername;
            
            @JsonProperty("portfolio_url")
            private String portfolioUrl;
            
            private String bio;
            private String location;
            
            private Links links;
            
            @JsonProperty("profile_image")
            private ProfileImage profileImage;
            
            @JsonProperty("instagram_username")
            private String instagramUsername;
            
            @JsonProperty("total_collections")
            private Integer totalCollections;
            
            @JsonProperty("total_likes")
            private Integer totalLikes;
            
            @JsonProperty("total_photos")
            private Integer totalPhotos;
            
            @JsonProperty("total_promoted_photos")
            private Integer totalPromotedPhotos;
            
            @JsonProperty("total_illustrations")
            private Integer totalIllustrations;
            
            @JsonProperty("total_promoted_illustrations")
            private Integer totalPromotedIllustrations;
            
            @JsonProperty("accepted_tos")
            private Boolean acceptedTos;
            
            @JsonProperty("for_hire")
            private Boolean forHire;
            
            private Social social;
            
            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class ProfileImage {
                private String small;
                private String medium;
                private String large;
            }
            
            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Social {
                @JsonProperty("instagram_username")
                private String instagramUsername;
                
                @JsonProperty("portfolio_url")
                private String portfolioUrl;
                
                @JsonProperty("twitter_username")
                private String twitterUsername;
                
                @JsonProperty("paypal_email")
                private String paypalEmail;
            }
        }
    }
} 