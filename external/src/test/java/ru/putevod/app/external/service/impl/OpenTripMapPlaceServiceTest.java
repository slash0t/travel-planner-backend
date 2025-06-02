package ru.putevod.app.external.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.putevod.app.external.config.AppConfig;
import ru.putevod.app.external.dto.PlaceRequestDto;
import ru.putevod.app.external.dto.PlaceResponseDto;
import ru.putevod.app.external.dto.response.PlaceSearchResponse;
import ru.putevod.app.external.dto.response.PlaceSuggestionResponse;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenTripMapPlaceServiceTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private AppConfig appConfig;

    @InjectMocks
    private OpenTripMapPlaceService service;

    private final String apiKey = "test-api-key";
    private final String baseUrl = "https://api.opentripmap.com/0.1/";

    @Test
    @DisplayName("searchPlaces - Success with coordinates")
    void searchPlaces_SuccessWithCoordinates() {
        setupAppConfig();
        setupWebClient();
        String query = "restaurant";
        Double lat = 55.7558;
        Double lon = 37.6173;
        Integer radius = 1000;
        Integer limit = 10;
        String category = "restaurants";

        Map<String, Object> place1 = createMockPlace("1", "Restaurant 1", "restaurants", 55.7558, 37.6173);
        Map<String, Object> place2 = createMockPlace("2", "Restaurant 2", "restaurants", 55.7559, 37.6174);

        when(responseSpec.bodyToMono(List.class)).thenReturn(Mono.just(Arrays.asList(place1, place2)));

        PlaceSearchResponse response = service.searchPlaces(query, lat, lon, radius, limit, category);

        assertNotNull(response);
        assertEquals(2, response.getTotal());
        assertEquals(2, response.getPlaces().size());
        assertEquals("Restaurant 1", response.getPlaces().get(0).getName());
        assertEquals("Restaurant 2", response.getPlaces().get(1).getName());
    }

    @Test
    @DisplayName("searchPlaces - Success without coordinates")
    void searchPlaces_SuccessWithoutCoordinates() {
        setupAppConfig();
        setupWebClient();
        String query = "Moscow";
        Map<String, Object> place = createMockPlace("1", "Moscow", "city", 55.7558, 37.6173);

        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(place));

        PlaceSearchResponse response = service.searchPlaces(query, null, null, null, null, null);

        assertNotNull(response);
        assertEquals(1, response.getTotal());
        assertEquals(1, response.getPlaces().size());
        assertEquals("Moscow", response.getPlaces().get(0).getName());
    }

    @Test
    @DisplayName("searchPlaces - Empty response")
    void searchPlaces_EmptyResponse() {
        setupAppConfig();
        setupWebClient();
        when(responseSpec.bodyToMono(List.class)).thenReturn(Mono.just(Collections.emptyList()));

        PlaceSearchResponse response = service.searchPlaces("query", 55.7558, 37.6173, 1000, 10, null);

        assertNotNull(response);
        assertEquals(0, response.getTotal());
        assertTrue(response.getPlaces().isEmpty());
    }

    @Test
    @DisplayName("getPlaceDetails - Success")
    void getPlaceDetails_Success() {
        setupAppConfig();
        setupWebClient();
        String placeId = "123";
        Map<String, Object> placeDetails = createDetailedPlace(placeId, "Test Place", "restaurants",
                55.7558, 37.6173, "Test description", "Test address", 4.5f, "123-456-789", "http://test.com");

        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(placeDetails));

        PlaceResponseDto response = service.getPlaceDetails(placeId);

        assertNotNull(response);
        assertEquals(placeId, response.getId());
        assertEquals("Test Place", response.getName());
        assertEquals("Test description", response.getDescription());
        assertEquals("Test address", response.getAddress());
        assertEquals(4.5f, response.getRating());
        assertEquals("123-456-789", response.getPhone());
        assertEquals("http://test.com", response.getWebsite());
        assertFalse(response.getPhotos().isEmpty());
    }

    @Test
    @DisplayName("getPlaceDetails - Not found")
    void getPlaceDetails_NotFound() {
        setupAppConfig();
        setupWebClient();
        String placeId = "123";
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.empty());

        PlaceResponseDto response = service.getPlaceDetails(placeId);

        assertNull(response);
    }

    @Test
    @DisplayName("autocompletePlaces - Success")
    void autocompletePlaces_Success() {
        setupAppConfig();
        setupWebClient();
        String input = "Moscow";
        Map<String, Object> suggestion1 = createMockPlace("1", "Moscow", "city", 55.7558, 37.6173);
        Map<String, Object> suggestion2 = createMockPlace("2", "Moscow City", "city", 55.7559, 37.6174);

        when(responseSpec.bodyToMono(List.class)).thenReturn(Mono.just(Arrays.asList(suggestion1, suggestion2)));

        PlaceSuggestionResponse response = service.autocompletePlaces(input, null, null, 10);

        assertNotNull(response);
        assertEquals(2, response.getSuggestions().size());
        assertEquals("Moscow", response.getSuggestions().get(0).getName());
        assertEquals("Moscow City", response.getSuggestions().get(1).getName());
    }

    @Test
    @DisplayName("autocompletePlaces - Empty response")
    void autocompletePlaces_EmptyResponse() {
        setupAppConfig();
        setupWebClient();
        when(responseSpec.bodyToMono(List.class)).thenReturn(Mono.just(Collections.emptyList()));

        PlaceSuggestionResponse response = service.autocompletePlaces("query", null, null, 10);

        assertNotNull(response);
        assertTrue(response.getSuggestions().isEmpty());
    }

    @Test
    @DisplayName("getNearbyPlaces - Success")
    void getNearbyPlaces_Success() {
        setupAppConfig();
        setupWebClient();
        Double lat = 55.7558;
        Double lon = 37.6173;
        Integer radius = 1000;
        Integer limit = 10;
        String categories = "restaurants,cafes";

        Map<String, Object> place1 = createMockPlace("1", "Restaurant 1", "restaurants", 55.7558, 37.6173);
        Map<String, Object> place2 = createMockPlace("2", "Cafe 1", "cafes", 55.7559, 37.6174);

        when(responseSpec.bodyToMono(List.class)).thenReturn(Mono.just(Arrays.asList(place1, place2)));

        PlaceSearchResponse response = service.getNearbyPlaces(lat, lon, radius, limit, categories);

        assertNotNull(response);
        assertEquals(2, response.getTotal());
        assertEquals(2, response.getPlaces().size());
        assertEquals("Restaurant 1", response.getPlaces().get(0).getName());
        assertEquals("Cafe 1", response.getPlaces().get(1).getName());
    }

    @Test
    @DisplayName("getAiRecommendations - Returns empty list")
    void getAiRecommendations_ReturnsEmptyList() {
        PlaceRequestDto request = new PlaceRequestDto();

        List<PlaceResponseDto> response = service.getAiRecommendations(request);

        assertNotNull(response);
        assertTrue(response.isEmpty());
    }

    @Test
    @DisplayName("geocodeAddress - Returns empty map")
    void geocodeAddress_ReturnsEmptyMap() {
        Map<String, Double> response = service.geocodeAddress("Moscow");

        assertNotNull(response);
        assertTrue(response.isEmpty());
    }

    private void setupAppConfig() {
        when(appConfig.getOpenTripMapApiKey()).thenReturn(apiKey);
        when(appConfig.getOpenTripMapBaseUrl()).thenReturn(baseUrl);
    }

    private void setupWebClient() {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }

    private Map<String, Object> createMockPlace(String id, String name, String category, Double lat, Double lon) {
        Map<String, Object> place = new HashMap<>();
        place.put("xid", id);
        place.put("name", name);
        place.put("kinds", category);

        Map<String, Object> point = new HashMap<>();
        point.put("lat", lat);
        point.put("lon", lon);
        place.put("point", point);

        Map<String, Object> preview = new HashMap<>();
        preview.put("source", "http://example.com/image.jpg");
        place.put("preview", preview);

        return place;
    }

    private Map<String, Object> createDetailedPlace(String id, String name, String category,
                                                    Double lat, Double lon, String description, String address, Float rating,
                                                    String phone, String website) {
        Map<String, Object> place = createMockPlace(id, name, category, lat, lon);

        Map<String, Object> wikipediaExtracts = new HashMap<>();
        wikipediaExtracts.put("text", description);
        place.put("wikipedia_extracts", wikipediaExtracts);

        Map<String, Object> addressData = new HashMap<>();
        addressData.put("road", address);
        place.put("address", addressData);

        place.put("rate", rating);
        place.put("phone", phone);
        place.put("url", website);

        return place;
    }
} 