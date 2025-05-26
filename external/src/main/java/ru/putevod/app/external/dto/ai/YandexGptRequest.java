package ru.putevod.app.external.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YandexGptRequest {
    
    @JsonProperty("modelUri")
    private String modelUri;
    
    @JsonProperty("completionOptions")
    private CompletionOptions completionOptions;
    
    private List<Message> messages;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompletionOptions {
        private boolean stream;
        private Double temperature;
        
        @JsonProperty("maxTokens")
        private String maxTokens;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        private String role;
        private String text;
    }
} 