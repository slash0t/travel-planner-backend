package ru.putevod.app.external.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import ru.putevod.app.external.config.AppConfig;
import ru.putevod.app.external.dto.PlaceRequestDto;
import ru.putevod.app.external.dto.PlaceResponseDto;
import ru.putevod.app.external.dto.PlaceSuggestionDto;
import ru.putevod.app.external.dto.PhotoDto;
import ru.putevod.app.external.dto.response.PlaceSearchResponse;
import ru.putevod.app.external.dto.response.PlaceSuggestionResponse;
import ru.putevod.app.external.service.PlaceService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OpenTripMapPlaceService implements PlaceService {

    private final WebClient webClient;
    private final AppConfig appConfig;
    
    public OpenTripMapPlaceService(WebClient webClient, AppConfig appConfig) {
        this.webClient = webClient;
        this.appConfig = appConfig;
    }

    @Override
    public PlaceSearchResponse searchPlaces(String query, Double lat, Double lon, Integer radius, Integer limit, String category) {
        log.info("Searching places with query={}, lat={}, lon={}, radius={}, limit={}, category={}", 
                query, lat, lon, radius, limit, category);
        
        if (lat == null || lon == null) {
            return searchPlacesByName(query, limit, category);
        }
        
        String url = UriComponentsBuilder
                .fromUriString(appConfig.getOpenTripMapBaseUrl())
                .path("ru/places/radius")
                .queryParam("apikey", appConfig.getOpenTripMapApiKey())
                .queryParam("radius", radius)
                .queryParam("limit", limit)
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .build()
                .toUriString();

        if (query != null && !query.isEmpty()) {
            url = UriComponentsBuilder.fromUriString(url)
                    .queryParam("name", query)
                    .build()
                    .toUriString();
        }

        if (category != null && !category.isEmpty()) {
            url = UriComponentsBuilder.fromUriString(url)
                    .queryParam("kinds", category)
                    .build()
                    .toUriString();
        }

        try {
            List<Map<String, Object>> response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();

            if (response == null || response.isEmpty()) {
                return PlaceSearchResponse.builder()
                        .places(new ArrayList<>())
                        .total(0)
                        .build();
            }

            List<PlaceResponseDto> places = response.stream()
                    .map(this::mapToPlaceResponse)
                    .collect(Collectors.toList());
                    
            return PlaceSearchResponse.builder()
                    .places(places)
                    .total(places.size())
                    .build();
                    
        } catch (Exception e) {
            log.error("Error searching places", e);
            return PlaceSearchResponse.builder()
                    .places(new ArrayList<>())
                    .total(0)
                    .build();
        }
    }
    
    private PlaceSearchResponse searchPlacesByName(String query, Integer limit, String category) {
        log.info("Searching places by name with query={}, limit={}, category={}", query, limit, category);
        
        String url = UriComponentsBuilder
                .fromUriString(appConfig.getOpenTripMapBaseUrl())
                .path("ru/places/geoname")
                .queryParam("apikey", appConfig.getOpenTripMapApiKey())
                .queryParam("name", query)
                .build()
                .toUriString();
        
        try {
            Map<String, Object> response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
                    
            if (response == null || !response.containsKey("name")) {
                return PlaceSearchResponse.builder()
                        .places(new ArrayList<>())
                        .total(0)
                        .build();
            }
            
            PlaceResponseDto place = PlaceResponseDto.builder()
                    .id(response.getOrDefault("xid", "").toString())
                    .name(response.getOrDefault("name", "").toString())
                    .lat(parseDouble(response, "lat"))
                    .lon(parseDouble(response, "lon"))
                    .address(response.containsKey("country") ? response.get("country").toString() : "")
                    .sourceSystem("OpenTripMap")
                    .build();
                    
            List<PlaceResponseDto> places = new ArrayList<>();
            places.add(place);
            
            return PlaceSearchResponse.builder()
                    .places(places)
                    .total(places.size())
                    .build();
                    
        } catch (Exception e) {
            log.error("Error searching places by name", e);
            return PlaceSearchResponse.builder()
                    .places(new ArrayList<>())
                    .total(0)
                    .build();
        }
    }

    @Override
    public PlaceResponseDto getPlaceDetails(String placeId) {
        log.info("Getting place details for id={}", placeId);
        
        String url = UriComponentsBuilder
                .fromUriString(appConfig.getOpenTripMapBaseUrl())
                .path("ru/places/xid/" + placeId)
                .queryParam("apikey", appConfig.getOpenTripMapApiKey())
                .build()
                .toUriString();

        try {
            Map<String, Object> response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) {
                return null;
            }

            return mapToPlaceDetailResponse(response);
        } catch (Exception e) {
            log.error("Error getting place details", e);
            return null;
        }
    }
    
    @Override
    public PlaceSuggestionResponse autocompletePlaces(String input, Double lat, Double lon, Integer limit) {
        log.info("Getting autocomplete suggestions for input={}, lat={}, lon={}, limit={}", input, lat, lon, limit);
        
        // OpenTripMap не имеет прямого API для автозаполнения, поэтому используем поиск по имени
        // и ограничиваем результаты
        String url = UriComponentsBuilder
                .fromUriString(appConfig.getOpenTripMapBaseUrl())
                .path("ru/places/autosuggest")
                .queryParam("apikey", appConfig.getOpenTripMapApiKey())
                .queryParam("name", input)
                .queryParam("limit", limit)
                .build()
                .toUriString();
                
        // Добавляем координаты, если они указаны
        if (lat != null && lon != null) {
            url = UriComponentsBuilder.fromUriString(url)
                    .queryParam("lat", lat)
                    .queryParam("lon", lon)
                    .build()
                    .toUriString();
        }
        
        try {
            List<Map<String, Object>> response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();
                    
            if (response == null || response.isEmpty()) {
                return PlaceSuggestionResponse.builder()
                        .suggestions(new ArrayList<>())
                        .build();
            }
            
            List<PlaceSuggestionDto> suggestions = response.stream()
                    .map(this::mapToPlaceSuggestion)
                    .collect(Collectors.toList());
                    
            return PlaceSuggestionResponse.builder()
                    .suggestions(suggestions)
                    .build();
                    
        } catch (Exception e) {
            log.error("Error getting autocomplete suggestions", e);
            return PlaceSuggestionResponse.builder()
                    .suggestions(new ArrayList<>())
                    .build();
        }
    }
    
    @Override
    public PlaceSearchResponse getNearbyPlaces(Double lat, Double lon, Integer radius, Integer limit, String categories) {
        log.info("Getting nearby places for lat={}, lon={}, radius={}, limit={}, categories={}", 
                lat, lon, radius, limit, categories);
        
        String url = UriComponentsBuilder
                .fromUriString(appConfig.getOpenTripMapBaseUrl())
                .path("ru/places/radius")
                .queryParam("apikey", appConfig.getOpenTripMapApiKey())
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .queryParam("radius", radius)
                .queryParam("limit", limit)
                .build()
                .toUriString();
                
        // Если указаны категории, добавляем их в запрос
        if (categories != null && !categories.isEmpty()) {
            url = UriComponentsBuilder.fromUriString(url)
                    .queryParam("kinds", categories)
                    .build()
                    .toUriString();
        }
        
        try {
            List<Map<String, Object>> response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();
                    
            if (response == null || response.isEmpty()) {
                return PlaceSearchResponse.builder()
                        .places(new ArrayList<>())
                        .total(0)
                        .build();
            }
            
            List<PlaceResponseDto> places = response.stream()
                    .map(this::mapToPlaceResponseWithDistance)
                    .collect(Collectors.toList());
                    
            return PlaceSearchResponse.builder()
                    .places(places)
                    .total(places.size())
                    .build();
                    
        } catch (Exception e) {
            log.error("Error getting nearby places", e);
            return PlaceSearchResponse.builder()
                    .places(new ArrayList<>())
                    .total(0)
                    .build();
        }
    }

    @Override
    public List<PlaceResponseDto> getAiRecommendations(PlaceRequestDto request) {
        // Заглушка для ИИ рекомендаций
        log.info("Getting AI recommendations for request: {}", request);
        
        // Возвращаем пустой список, так как это заглушка
        return new ArrayList<>();
    }

    @Override
    public Map<String, Double> geocodeAddress(String address) {
        log.warn("Геокодирование адреса не поддерживается в OpenTripMapPlaceService");
        return Collections.emptyMap();
    }

    private PlaceResponseDto mapToPlaceResponse(Map<String, Object> data) {
        return PlaceResponseDto.builder()
                .id(data.getOrDefault("xid", "").toString())
                .name(data.getOrDefault("name", "").toString())
                .category(data.getOrDefault("kinds", "").toString())
                .lat(parseDouble(data.get("point"), "lat"))
                .lon(parseDouble(data.get("point"), "lon"))
                .previewUrl(getPreviewUrl(data))
                .sourceSystem("OpenTripMap")
                .build();
    }
    
    private PlaceResponseDto mapToPlaceResponseWithDistance(Map<String, Object> data) {
        PlaceResponseDto dto = mapToPlaceResponse(data);
        
        // Добавляем информацию о расстоянии, если она есть
        if (data.containsKey("dist")) {
            Integer distanceMeters = parseInteger(data, "dist");
            dto.setDistanceMeters(distanceMeters);
        }
        
        return dto;
    }

    private PlaceResponseDto mapToPlaceDetailResponse(Map<String, Object> data) {
        String previewUrl = null;
        if (data.containsKey("preview") && data.get("preview") instanceof Map) {
            Map<String, Object> preview = (Map<String, Object>) data.get("preview");
            previewUrl = preview.getOrDefault("source", "").toString();
        }

        String address = "";
        if (data.containsKey("address") && data.get("address") instanceof Map) {
            Map<String, Object> addressData = (Map<String, Object>) data.get("address");
            address = formatAddress(addressData);
        }
        
        // Получаем список фотографий
        List<PhotoDto> photos = new ArrayList<>();
        if (previewUrl != null && !previewUrl.isEmpty()) {
            PhotoDto photo = PhotoDto.builder()
                    .url(previewUrl)
                    .width(800) // Примерные размеры для превью
                    .height(600)
                    .build();
            photos.add(photo);
        }
        
        // Получаем веб-сайт, если он есть
        String website = data.containsKey("url") ? data.getOrDefault("url", "").toString() : null;
        
        // Получаем телефон, если он есть
        String phone = data.containsKey("phone") ? data.getOrDefault("phone", "").toString() : null;
        
        // Преобразуем рейтинг в формат float
        Float rating = null;
        if (data.containsKey("rate") && data.get("rate") instanceof Number) {
            rating = ((Number) data.get("rate")).floatValue();
        }

        return PlaceResponseDto.builder()
                .id(data.getOrDefault("xid", "").toString())
                .name(data.getOrDefault("name", "").toString())
                .description(data.containsKey("wikipedia_extracts") && data.get("wikipedia_extracts") instanceof Map
                        ? ((Map<String, Object>) data.get("wikipedia_extracts")).getOrDefault("text", "").toString()
                        : "")
                .address(address)
                .previewUrl(previewUrl)
                .category(data.getOrDefault("kinds", "").toString())
                .lat(parseDouble(data, "lat"))
                .lon(parseDouble(data, "lon"))
                .rating(rating)
                .phone(phone)
                .website(website)
                .photos(photos)
                .sourceSystem("OpenTripMap")
                .externalId(data.getOrDefault("xid", "").toString())
                .externalUrl(website)
                .build();
    }
    
    private PlaceSuggestionDto mapToPlaceSuggestion(Map<String, Object> data) {
        return PlaceSuggestionDto.builder()
                .id(data.getOrDefault("xid", "").toString())
                .name(data.getOrDefault("name", "").toString())
                .category(data.getOrDefault("kinds", "").toString())
                .previewUrl(getPreviewUrl(data))
                .address(formatShortAddress(data))
                .build();
    }
    
    private String getPreviewUrl(Map<String, Object> data) {
        // Логика получения URL превью из данных
        if (data.containsKey("preview") && data.get("preview") instanceof Map) {
            Map<String, Object> preview = (Map<String, Object>) data.get("preview");
            return preview.getOrDefault("source", "").toString();
        }
        
        return null;
    }
    
    private String formatAddress(Map<String, Object> addressData) {
        StringBuilder address = new StringBuilder();
        
        // Формируем полный адрес из доступных компонентов
        if (addressData.containsKey("house_number")) {
            address.append(addressData.get("house_number")).append(", ");
        }
        
        if (addressData.containsKey("road")) {
            address.append(addressData.get("road")).append(", ");
        }
        
        if (addressData.containsKey("suburb")) {
            address.append(addressData.get("suburb")).append(", ");
        }
        
        if (addressData.containsKey("city")) {
            address.append(addressData.get("city")).append(", ");
        }
        
        if (addressData.containsKey("state")) {
            address.append(addressData.get("state")).append(", ");
        }
        
        if (addressData.containsKey("country")) {
            address.append(addressData.get("country"));
        }
        
        String result = address.toString();
        
        // Убираем завершающую запятую, если она есть
        if (result.endsWith(", ")) {
            result = result.substring(0, result.length() - 2);
        }
        
        return result;
    }
    
    private String formatShortAddress(Map<String, Object> data) {
        // Извлекаем краткий адрес или формируем его из названия и категории
        if (data.containsKey("address") && data.get("address") instanceof Map) {
            Map<String, Object> addressData = (Map<String, Object>) data.get("address");
            
            if (addressData.containsKey("city")) {
                return addressData.get("city").toString();
            }
        }
        
        // Если адрес не найден, возвращаем пустую строку
        return "";
    }

    private Double parseDouble(Object data, String field) {
        if (data instanceof Map) {
            Object value = ((Map<?, ?>) data).get(field);
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
        }
        return null;
    }

    private Integer parseInteger(Map<String, Object> data, String field) {
        if (data.containsKey(field) && data.get(field) instanceof Number) {
            return ((Number) data.get(field)).intValue();
        }
        return null;
    }
} 