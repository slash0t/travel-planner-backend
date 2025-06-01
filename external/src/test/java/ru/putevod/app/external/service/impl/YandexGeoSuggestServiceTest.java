package ru.putevod.app.external.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.putevod.app.external.config.AppConfig;
import ru.putevod.app.external.dto.PlaceSuggestionDto;
import ru.putevod.app.external.dto.response.PlaceSuggestionResponse;
import ru.putevod.app.external.dto.response.YandexGeocoderResponse;
import ru.putevod.app.external.dto.response.YandexGeoSuggestResponse;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class YandexGeoSuggestServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private AppConfig appConfig;

    @Mock
    private OpenTripMapPlaceService fallbackService;

    @InjectMocks
    private YandexGeoSuggestService service;

    private static final String API_KEY = "test-api-key";
    private static final String BASE_URL = "https://test.yandex.ru/v1/suggest";
    private static final String GEOCODER_API_KEY = "test-geocoder-key";
    private static final String GEOCODER_BASE_URL = "https://test.geocode.yandex.ru/v1/";

    @Test
    @DisplayName("autocompletePlaces - Null input")
    void autocompletePlaces_NullInput() {
        PlaceSuggestionResponse response = service.autocompletePlaces(null, null, null, 10);

        assertNotNull(response);
        assertTrue(response.getSuggestions().isEmpty());
    }

    @Test
    @DisplayName("autocompletePlaces - Empty input")
    void autocompletePlaces_EmptyInput() {
        PlaceSuggestionResponse response = service.autocompletePlaces("", null, null, 10);

        assertNotNull(response);
        assertTrue(response.getSuggestions().isEmpty());
    }

    @Test
    @DisplayName("autocompletePlaces - No API key")
    void autocompletePlaces_NoApiKey() {
        when(appConfig.getYandexGeoSuggestApiKey()).thenReturn("");
        PlaceSuggestionResponse fallbackResponse = PlaceSuggestionResponse.builder()
                .suggestions(Collections.singletonList(PlaceSuggestionDto.builder()
                        .id("test-id")
                        .name("Test Place")
                        .build()))
                .build();
        when(fallbackService.autocompletePlaces("Moscow", null, null, 10))
                .thenReturn(fallbackResponse);

        PlaceSuggestionResponse response = service.autocompletePlaces("Moscow", null, null, 10);

        assertNotNull(response);
        assertEquals(1, response.getSuggestions().size());
        assertEquals("test-id", response.getSuggestions().get(0).getId());
        assertEquals("Test Place", response.getSuggestions().get(0).getName());
    }

    @Test
    @DisplayName("autocompletePlaces - Successful response")
    void autocompletePlaces_SuccessfulResponse() {
        when(appConfig.getYandexGeoSuggestApiKey()).thenReturn(API_KEY);
        when(appConfig.getYandexGeoSuggestBaseUrl()).thenReturn(BASE_URL);

        YandexGeoSuggestResponse.GeoSuggestItem item = createMockGeoSuggestItem(
                "Москва",
                "Россия, Москва",
                Arrays.asList("город", "столица"),
                1000.0,
                55.7558,
                37.6173
        );

        YandexGeoSuggestResponse mockResponse = new YandexGeoSuggestResponse();
        mockResponse.setRequestId("test-req-id");
        mockResponse.setResults(Collections.singletonList(item));

        when(restTemplate.getForEntity(any(String.class), eq(YandexGeoSuggestResponse.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        PlaceSuggestionResponse response = service.autocompletePlaces("Moscow", null, null, 10);

        assertNotNull(response);
        assertEquals(1, response.getSuggestions().size());
        
        PlaceSuggestionDto suggestion = response.getSuggestions().get(0);
        assertEquals("москва", suggestion.getId());
        assertEquals("Москва", suggestion.getName());
        assertEquals("Россия, Москва (1000.0 м)", suggestion.getAddress());
        assertEquals("город, столица", suggestion.getCategory());
        assertEquals(55.7558, suggestion.getLat());
        assertEquals(37.6173, suggestion.getLon());
    }

    @Test
    @DisplayName("autocompletePlaces - API error response")
    void autocompletePlaces_ApiErrorResponse() {
        when(appConfig.getYandexGeoSuggestApiKey()).thenReturn(API_KEY);
        when(appConfig.getYandexGeoSuggestBaseUrl()).thenReturn(BASE_URL);
        when(restTemplate.getForEntity(any(String.class), eq(YandexGeoSuggestResponse.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        PlaceSuggestionResponse fallbackResponse = PlaceSuggestionResponse.builder()
                .suggestions(Collections.singletonList(PlaceSuggestionDto.builder()
                        .id("test-id")
                        .name("Test Place")
                        .build()))
                .build();
        when(fallbackService.autocompletePlaces("Moscow", null, null, 10))
                .thenReturn(fallbackResponse);

        PlaceSuggestionResponse response = service.autocompletePlaces("Moscow", null, null, 10);

        assertNotNull(response);
        assertEquals(1, response.getSuggestions().size());
        assertEquals("test-id", response.getSuggestions().get(0).getId());
        assertEquals("Test Place", response.getSuggestions().get(0).getName());
    }

    @Test
    @DisplayName("autocompletePlaces - RestClientException")
    void autocompletePlaces_RestClientException() {
        when(appConfig.getYandexGeoSuggestApiKey()).thenReturn(API_KEY);
        when(appConfig.getYandexGeoSuggestBaseUrl()).thenReturn(BASE_URL);
        when(restTemplate.getForEntity(any(String.class), eq(YandexGeoSuggestResponse.class)))
                .thenThrow(new RestClientException("API error"));

        PlaceSuggestionResponse fallbackResponse = PlaceSuggestionResponse.builder()
                .suggestions(Collections.singletonList(PlaceSuggestionDto.builder()
                        .id("test-id")
                        .name("Test Place")
                        .build()))
                .build();
        when(fallbackService.autocompletePlaces("Moscow", null, null, 10))
                .thenReturn(fallbackResponse);

        PlaceSuggestionResponse response = service.autocompletePlaces("Moscow", null, null, 10);

        assertNotNull(response);
        assertEquals(1, response.getSuggestions().size());
        assertEquals("test-id", response.getSuggestions().get(0).getId());
        assertEquals("Test Place", response.getSuggestions().get(0).getName());
    }

    @Test
    @DisplayName("geocodeAddress - Null address")
    void geocodeAddress_NullAddress() {
        Map<String, Double> coordinates = service.geocodeAddress(null);

        assertNotNull(coordinates);
        assertTrue(coordinates.isEmpty());
    }

    @Test
    @DisplayName("geocodeAddress - Empty address")
    void geocodeAddress_EmptyAddress() {
        Map<String, Double> coordinates = service.geocodeAddress("");

        assertNotNull(coordinates);
        assertTrue(coordinates.isEmpty());
    }

    @Test
    @DisplayName("geocodeAddress - No API key")
    void geocodeAddress_NoApiKey() {
        when(appConfig.getYandexGeocoderApiKey()).thenReturn("");

        Map<String, Double> coordinates = service.geocodeAddress("Moscow");

        assertNotNull(coordinates);
        assertTrue(coordinates.isEmpty());
    }

    @Test
    @DisplayName("geocodeAddress - Successful response")
    void geocodeAddress_SuccessfulResponse() {
        when(appConfig.getYandexGeocoderApiKey()).thenReturn(GEOCODER_API_KEY);
        when(appConfig.getYandexGeocoderBaseUrl()).thenReturn(GEOCODER_BASE_URL);
        YandexGeocoderResponse mockResponse = createMockGeocoderResponse(55.7558, 37.6173);
        when(restTemplate.getForEntity(any(String.class), eq(YandexGeocoderResponse.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        Map<String, Double> coordinates = service.geocodeAddress("Moscow");

        assertNotNull(coordinates);
        assertEquals(2, coordinates.size());
        assertEquals(55.7558, coordinates.get("lat"));
        assertEquals(37.6173, coordinates.get("lon"));
    }

    @Test
    @DisplayName("geocodeAddress - API error response")
    void geocodeAddress_ApiErrorResponse() {
        when(appConfig.getYandexGeocoderApiKey()).thenReturn(GEOCODER_API_KEY);
        when(appConfig.getYandexGeocoderBaseUrl()).thenReturn(GEOCODER_BASE_URL);
        when(restTemplate.getForEntity(any(String.class), eq(YandexGeocoderResponse.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        Map<String, Double> coordinates = service.geocodeAddress("Moscow");

        assertNotNull(coordinates);
        assertTrue(coordinates.isEmpty());
    }

    @Test
    @DisplayName("geocodeAddress - RestClientException")
    void geocodeAddress_RestClientException() {
        when(appConfig.getYandexGeocoderApiKey()).thenReturn(GEOCODER_API_KEY);
        when(appConfig.getYandexGeocoderBaseUrl()).thenReturn(GEOCODER_BASE_URL);
        when(restTemplate.getForEntity(any(String.class), eq(YandexGeocoderResponse.class)))
                .thenThrow(new RestClientException("API error"));

        Map<String, Double> coordinates = service.geocodeAddress("Moscow");

        assertNotNull(coordinates);
        assertTrue(coordinates.isEmpty());
    }

    @Test
    @DisplayName("geocodeAddress - Empty response")
    void geocodeAddress_EmptyResponse() {
        when(appConfig.getYandexGeocoderApiKey()).thenReturn(GEOCODER_API_KEY);
        when(appConfig.getYandexGeocoderBaseUrl()).thenReturn(GEOCODER_BASE_URL);
        YandexGeocoderResponse mockResponse = new YandexGeocoderResponse();
        when(restTemplate.getForEntity(any(String.class), eq(YandexGeocoderResponse.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        Map<String, Double> coordinates = service.geocodeAddress("Moscow");

        assertNotNull(coordinates);
        assertTrue(coordinates.isEmpty());
    }

    private YandexGeoSuggestResponse.GeoSuggestItem createMockGeoSuggestItem(
            String title, String subtitle, List<String> tags, Double distance, Double lat, Double lon) {
        YandexGeoSuggestResponse.GeoSuggestItem item = new YandexGeoSuggestResponse.GeoSuggestItem();
        
        YandexGeoSuggestResponse.TextWithHighlight titleObj = new YandexGeoSuggestResponse.TextWithHighlight();
        titleObj.setText(title);
        item.setTitle(titleObj);
        
        YandexGeoSuggestResponse.TextWithHighlight subtitleObj = new YandexGeoSuggestResponse.TextWithHighlight();
        subtitleObj.setText(subtitle);
        item.setSubtitle(subtitleObj);
        
        item.setTags(tags);
        
        YandexGeoSuggestResponse.Distance distanceObj = new YandexGeoSuggestResponse.Distance();
        distanceObj.setValue(distance);
        distanceObj.setText(distance + " м");
        item.setDistance(distanceObj);
        
        YandexGeoSuggestResponse.Geometry geometry = new YandexGeoSuggestResponse.Geometry();
        YandexGeoSuggestResponse.Location location = new YandexGeoSuggestResponse.Location();
        location.setLat(lat);
        location.setLon(lon);
        geometry.setLocation(location);
        item.setGeometry(geometry);
        
        return item;
    }

    private YandexGeocoderResponse createMockGeocoderResponse(Double lat, Double lon) {
        YandexGeocoderResponse response = new YandexGeocoderResponse();
        YandexGeocoderResponse.GeocoderResponse geocoderResponse = new YandexGeocoderResponse.GeocoderResponse();
        YandexGeocoderResponse.GeoObjectCollection collection = new YandexGeocoderResponse.GeoObjectCollection();
        
        YandexGeocoderResponse.FeatureMember member = new YandexGeocoderResponse.FeatureMember();
        YandexGeocoderResponse.GeoObject geoObject = new YandexGeocoderResponse.GeoObject();
        YandexGeocoderResponse.Point point = new YandexGeocoderResponse.Point();
        point.setPos(lon + " " + lat);
        geoObject.setPoint(point);
        member.setGeoObject(geoObject);
        
        collection.setFeatureMembers(Collections.singletonList(member));
        geocoderResponse.setGeoObjectCollection(collection);
        response.setResponse(geocoderResponse);
        
        return response;
    }
} 