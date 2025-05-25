package ru.putevod.app.external.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.putevod.app.external.config.AppConfig;
import ru.putevod.app.external.dto.PlaceRequestDto;
import ru.putevod.app.external.dto.PlaceResponseDto;
import ru.putevod.app.external.dto.PlaceSuggestionDto;
import ru.putevod.app.external.dto.response.PlaceSearchResponse;
import ru.putevod.app.external.dto.response.PlaceSuggestionResponse;
import ru.putevod.app.external.dto.response.YandexGeoSuggestResponse;
import ru.putevod.app.external.service.PlaceService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class YandexGeoSuggestService implements PlaceService {
    
    private final RestTemplate restTemplate;
    private final AppConfig appConfig;
    private final OpenTripMapPlaceService fallbackService;
    
    @Override
    public PlaceSuggestionResponse autocompletePlaces(String input, Double lat, Double lon, Integer limit) {
        if (input == null || input.trim().isEmpty()) {
            log.warn("Пустой запрос для автодополнения");
            return PlaceSuggestionResponse.builder()
                    .suggestions(Collections.emptyList())
                    .build();
        }
        
        if (appConfig.getYandexGeoSuggestApiKey() == null || appConfig.getYandexGeoSuggestApiKey().isEmpty()) {
            log.warn("Ключ API Яндекс Геосаджест не настроен, используем запасной сервис");
            return fallbackService.autocompletePlaces(input, lat, lon, limit);
        }
        
        try {
            String encodedText = URLEncoder.encode(input, StandardCharsets.UTF_8);
            UriComponentsBuilder uriBuilder = UriComponentsBuilder
                    .fromUriString(appConfig.getYandexGeoSuggestBaseUrl())
                    .queryParam("apikey", appConfig.getYandexGeoSuggestApiKey())
                    .queryParam("text", encodedText)
                    .queryParam("lang", "ru_RU")
                    .queryParam("type", "geo")
                    .queryParam("results", limit);
            
            if (lat != null && lon != null) {
                uriBuilder.queryParam("ll", lon + "," + lat);  // Яндекс API принимает координаты в формате "lon,lat"
            }
            
            String url = uriBuilder.build().toUriString();
            log.debug("Запрос к API Яндекс Геосаджест: {}", url);
            
            ResponseEntity<YandexGeoSuggestResponse> response = restTemplate.getForEntity(
                    url,
                    YandexGeoSuggestResponse.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<PlaceSuggestionDto> suggestions = mapYandexResponseToSuggestions(response.getBody());
                log.info("Получено {} предложений автодополнения от Яндекс Геосаджест", suggestions.size());
                
                return PlaceSuggestionResponse.builder()
                        .suggestions(suggestions)
                        .build();
            } else {
                log.warn("API Яндекс Геосаджест вернул ошибку: {}", response.getStatusCode());
                return fallbackService.autocompletePlaces(input, lat, lon, limit);
            }
        } catch (RestClientException e) {
            log.error("Ошибка при запросе к API Яндекс Геосаджест: {}", e.getMessage());
            return fallbackService.autocompletePlaces(input, lat, lon, limit);
        } catch (Exception e) {
            log.error("Неожиданная ошибка при запросе к API Яндекс Геосаджест: {}", e.getMessage());
            return fallbackService.autocompletePlaces(input, lat, lon, limit);
        }
    }
    
    private List<PlaceSuggestionDto> mapYandexResponseToSuggestions(YandexGeoSuggestResponse response) {
        if (response.getItems() == null || response.getItems().isEmpty()) {
            return Collections.emptyList();
        }
        
        return response.getItems().stream()
                .map(this::mapToPlaceSuggestion)
                .collect(Collectors.toList());
    }
    
    private PlaceSuggestionDto mapToPlaceSuggestion(YandexGeoSuggestResponse.GeoSuggestItem item) {
        String category = item.getTags() != null && !item.getTags().isEmpty() 
                ? String.join(", ", item.getTags()) 
                : (item.getType() != null ? item.getType() : "");
        
        return PlaceSuggestionDto.builder()
                .id(item.getId())
                .name(item.getTitle())
                .address(item.getSubtitle())
                .category(category)
                .build();
    }

    @Override
    public PlaceSearchResponse searchPlaces(String query, Double lat, Double lon, Integer radius, Integer limit, String category) {
        return fallbackService.searchPlaces(query, lat, lon, radius, limit, category);
    }

    @Override
    public PlaceResponseDto getPlaceDetails(String placeId) {
        return fallbackService.getPlaceDetails(placeId);
    }

    @Override
    public PlaceSearchResponse getNearbyPlaces(Double lat, Double lon, Integer radius, Integer limit, String categories) {
        return fallbackService.getNearbyPlaces(lat, lon, radius, limit, categories);
    }

    @Override
    public List<PlaceResponseDto> getAiRecommendations(PlaceRequestDto request) {
        return fallbackService.getAiRecommendations(request);
    }
} 