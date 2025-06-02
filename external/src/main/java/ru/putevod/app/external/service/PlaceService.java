package ru.putevod.app.external.service;

import ru.putevod.app.external.dto.PlaceRequestDto;
import ru.putevod.app.external.dto.PlaceResponseDto;
import ru.putevod.app.external.dto.response.PlaceSearchResponse;
import ru.putevod.app.external.dto.response.PlaceSuggestionResponse;

import java.util.List;
import java.util.Map;

public interface PlaceService {
    /**
     * Поиск мест по названию и координатам
     */
    PlaceSearchResponse searchPlaces(String query, Double lat, Double lon, Integer radius, Integer limit, String category);

    /**
     * Получение детальной информации о месте
     */
    PlaceResponseDto getPlaceDetails(String placeId);

    /**
     * Автозаполнение для поиска мест
     */
    PlaceSuggestionResponse autocompletePlaces(String input, Double lat, Double lon, Integer limit);

    /**
     * Поиск мест поблизости
     */
    PlaceSearchResponse getNearbyPlaces(Double lat, Double lon, Integer radius, Integer limit, String categories);

    /**
     * ИИ рекомендации (заглушка)
     */
    List<PlaceResponseDto> getAiRecommendations(PlaceRequestDto request);

    /**
     * Геокодирование адреса (получение координат по адресу)
     *
     * @param address Адрес или название места для геокодирования
     * @return Карта с координатами (ключи "lat" и "lon")
     */
    Map<String, Double> geocodeAddress(String address);
} 