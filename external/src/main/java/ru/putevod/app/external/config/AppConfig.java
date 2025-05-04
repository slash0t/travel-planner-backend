package ru.putevod.app.external.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.external-api")
@Data
public class AppConfig {
    private String openTripMapApiKey;
    private String openTripMapBaseUrl = "https://api.opentripmap.com/0.1/";
} 