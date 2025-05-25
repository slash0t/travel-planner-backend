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
import ru.putevod.app.external.dto.response.YandexGeocoderResponse;
import ru.putevod.app.external.dto.response.YandexGeoSuggestResponse;
import ru.putevod.app.external.service.PlaceService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            UriComponentsBuilder uriBuilder = UriComponentsBuilder
                    .fromUriString(appConfig.getYandexGeoSuggestBaseUrl())
                    .queryParam("apikey", appConfig.getYandexGeoSuggestApiKey())
                    .queryParam("text", input.trim())
                    .queryParam("lang", "ru_RU")
                    .queryParam("type", "geo")
                    .queryParam("results", limit);
            
            if (lat != null && lon != null) {
                uriBuilder.queryParam("ll", lon + "," + lat);
            }
            
            String url = uriBuilder.build().toUriString();
            log.debug("Запрос к API Яндекс Геосаджест: {}", url);
            
            ResponseEntity<YandexGeoSuggestResponse> response = restTemplate.getForEntity(
                    url,
                    YandexGeoSuggestResponse.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                YandexGeoSuggestResponse responseBody = response.getBody();
                log.debug("Получен ответ от API Яндекс Геосаджест: reqId={}, результатов={}", 
                        responseBody.getRequestId(),
                        responseBody.getResults() != null ? responseBody.getResults().size() : 0);
                
                List<PlaceSuggestionDto> suggestions = mapYandexResponseToSuggestions(responseBody);
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
            log.error("Неожиданная ошибка при запросе к API Яндекс Геосаджест: {}", e.getMessage(), e);
            return fallbackService.autocompletePlaces(input, lat, lon, limit);
        }
    }
    
    private List<PlaceSuggestionDto> mapYandexResponseToSuggestions(YandexGeoSuggestResponse response) {
        if (response.getResults() == null || response.getResults().isEmpty()) {
            log.debug("Ответ API Яндекс Геосаджест не содержит результатов");
            return Collections.emptyList();
        }
        
        List<PlaceSuggestionDto> suggestions = response.getResults().stream()
                .map(this::mapToPlaceSuggestion)
                .collect(Collectors.toList());
        
        log.debug("Преобразовано {} результатов в предложения автодополнения", suggestions.size());
        return suggestions;
    }
    
    private PlaceSuggestionDto mapToPlaceSuggestion(YandexGeoSuggestResponse.GeoSuggestItem item) {
        String title = item.getTitle() != null ? item.getTitle().getText() : "";
        String subtitle = item.getSubtitle() != null ? item.getSubtitle().getText() : "";
        String category = item.getTags() != null && !item.getTags().isEmpty() 
                ? String.join(", ", item.getTags()) 
                : "";
        
        String distanceText = "";
        if (item.getDistance() != null && item.getDistance().getText() != null) {
            distanceText = " (" + item.getDistance().getText() + ")";
        }
        
        // Извлекаем координаты, если они есть
        Double latitude = null;
        Double longitude = null;
        if (item.getGeometry() != null && item.getGeometry().getLocation() != null) {
            latitude = item.getGeometry().getLocation().getLat();
            longitude = item.getGeometry().getLocation().getLon();
            log.debug("Извлечены координаты для места {}: lat={}, lon={}", title, latitude, longitude);
        } else {
            log.debug("Координаты для места {} отсутствуют в ответе", title);
        }
        
        PlaceSuggestionDto suggestion = PlaceSuggestionDto.builder()
                .id(title.replaceAll("\\s+", "-").toLowerCase()) // Генерируем ID из названия, т.к. у Яндекса нет явного ID
                .name(title)
                .address(subtitle + distanceText)
                .category(category)
                .lat(latitude)
                .lon(longitude)
                .build();
        
        log.debug("Преобразован результат: {} -> {}", title, suggestion);
        return suggestion;
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

    @Override
    public Map<String, Double> geocodeAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            log.warn("Пустой адрес для геокодирования");
            return Collections.emptyMap();
        }
        
        if (appConfig.getYandexGeocoderApiKey() == null || appConfig.getYandexGeocoderApiKey().isEmpty()) {
            log.warn("Ключ API Яндекс Геокодера не настроен, геокодирование невозможно");
            return Collections.emptyMap();
        }
        
        try {
            String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
            UriComponentsBuilder uriBuilder = UriComponentsBuilder
                    .fromUriString(appConfig.getYandexGeocoderBaseUrl())
                    .queryParam("apikey", appConfig.getYandexGeocoderApiKey())
                    .queryParam("geocode", encodedAddress)
                    .queryParam("format", "json")
                    .queryParam("results", 1);
            
            String url = uriBuilder.build().toUriString();
            log.debug("Запрос к API Яндекс Геокодер: {}", url);
            
            ResponseEntity<YandexGeocoderResponse> response = restTemplate.getForEntity(
                    url,
                    YandexGeocoderResponse.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                YandexGeocoderResponse responseBody = response.getBody();
                return extractCoordinatesFromGeocoderResponse(responseBody, address);
            } else {
                log.warn("API Яндекс Геокодер вернул ошибку: {}", response.getStatusCode());
                return Collections.emptyMap();
            }
        } catch (RestClientException e) {
            log.error("Ошибка при запросе к API Яндекс Геокодер: {}", e.getMessage());
            return Collections.emptyMap();
        } catch (Exception e) {
            log.error("Неожиданная ошибка при запросе к API Яндекс Геокодер: {}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }
    
    private Map<String, Double> extractCoordinatesFromGeocoderResponse(YandexGeocoderResponse response, String address) {
        Map<String, Double> coordinates = new HashMap<>();
        
        if (response.getResponse() == null 
                || response.getResponse().getGeoObjectCollection() == null 
                || response.getResponse().getGeoObjectCollection().getFeatureMembers() == null 
                || response.getResponse().getGeoObjectCollection().getFeatureMembers().isEmpty()) {
            log.warn("Адрес '{}' не найден в ответе геокодера", address);
            return coordinates;
        }

        YandexGeocoderResponse.FeatureMember firstFeature = response.getResponse().getGeoObjectCollection().getFeatureMembers().get(0);
        if (firstFeature.getGeoObject() == null || firstFeature.getGeoObject().getPoint() == null) {
            log.warn("В ответе геокодера отсутствуют координаты для адреса '{}'", address);
            return coordinates;
        }

        String pos = firstFeature.getGeoObject().getPoint().getPos();
        if (pos == null || pos.trim().isEmpty()) {
            log.warn("Пустые координаты в ответе геокодера для адреса '{}'", address);
            return coordinates;
        }
        
        try {
            String[] parts = pos.split(" ");
            if (parts.length >= 2) {
                // Порядок координат в ответе: долгота широта
                double lon = Double.parseDouble(parts[0]);
                double lat = Double.parseDouble(parts[1]);
                
                coordinates.put("lon", lon);
                coordinates.put("lat", lat);
                
                log.info("Получены координаты для адреса '{}': lat={}, lon={}", address, lat, lon);
            } else {
                log.warn("Некорректный формат координат в ответе геокодера для адреса '{}': {}", address, pos);
            }
        } catch (NumberFormatException e) {
            log.error("Ошибка при парсинге координат для адреса '{}': {}", address, e.getMessage());
        }
        
        return coordinates;
    }
} 