package ru.putevod.app.external.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;
import ru.putevod.app.external.service.PlaceService;
import ru.putevod.app.external.service.impl.OpenTripMapPlaceService;
import ru.putevod.app.external.service.impl.YandexGeoSuggestService;

@Configuration
public class PlaceServiceConfig {
    
    @Bean
    public OpenTripMapPlaceService openTripMapPlaceService(RestTemplate restTemplate, AppConfig appConfig) {
        return new OpenTripMapPlaceService(restTemplate, appConfig);
    }
    
    @Bean
    @Primary
    public PlaceService placeService(RestTemplate restTemplate, AppConfig appConfig, OpenTripMapPlaceService fallbackService) {
        return new YandexGeoSuggestService(restTemplate, appConfig, fallbackService);
    }
} 