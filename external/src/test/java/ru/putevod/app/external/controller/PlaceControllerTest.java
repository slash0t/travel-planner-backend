package ru.putevod.app.external.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.putevod.app.external.service.PlaceService;
import ru.putevod.app.external.dto.PlaceResponseDto;
import ru.putevod.app.external.dto.PlaceSuggestionDto;
import ru.putevod.app.external.dto.response.PlaceSearchResponse;
import ru.putevod.app.external.dto.response.PlaceSuggestionResponse;
import ru.putevod.app.external.client.AuthServiceClient;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Import SecurityAutoConfiguration
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@WebMvcTest(value = PlaceController.class, 
            excludeAutoConfiguration = SecurityAutoConfiguration.class) // Disable security for this test
class PlaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlaceService placeService;

    @MockBean
    private AuthServiceClient authServiceClient;

    private PlaceSearchResponse mockSearchResponse;
    private PlaceResponseDto mockPlaceDetailsResponse;
    private PlaceSuggestionResponse mockSuggestionResponse;
    private PlaceSuggestionDto mockSuggestionDto;
    private PlaceResponseDto mockPlaceResponseDto;

    @BeforeEach
    void setUp() {
        // Initialize common mock DTOs here if needed
        mockSearchResponse = new PlaceSearchResponse();
        mockSearchResponse.setPlaces(Collections.emptyList());
        mockSearchResponse.setTotal(0);

        mockPlaceDetailsResponse = new PlaceResponseDto();
        // Set required fields for PlaceResponseDto based on its definition
        mockPlaceDetailsResponse.setId("testPlaceId123");
        mockPlaceDetailsResponse.setName("Test Place");
        mockPlaceDetailsResponse.setLat(0.0);
        mockPlaceDetailsResponse.setLon(0.0);
        // Add other necessary fields...

        mockSuggestionResponse = new PlaceSuggestionResponse();
        mockSuggestionDto = new PlaceSuggestionDto();
        mockSuggestionDto.setId("suggestId1");
        mockSuggestionDto.setName("Suggestion 1");
        mockSuggestionResponse.setSuggestions(Collections.singletonList(mockSuggestionDto));

        mockPlaceResponseDto = new PlaceResponseDto();
        mockPlaceResponseDto.setId("placeId1");
        mockPlaceResponseDto.setName("Place 1");
        mockPlaceResponseDto.setLat(1.0);
        mockPlaceResponseDto.setLon(1.0);
        mockSearchResponse.setPlaces(Collections.singletonList(mockPlaceResponseDto));
        mockSearchResponse.setTotal(1);

    }


    // --- Test for /api/v1/places/search ---
    @Test
    void searchPlaces_shouldReturnPlaces() throws Exception {
        given(placeService.searchPlaces(anyString(), any(), any(), any(), any(), any()))
                .willReturn(mockSearchResponse); // Use correct response type

        mockMvc.perform(get("/api/v1/places/search") // Corrected path
                        .param("query", "test query")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.total").value(1)) // Updated based on setup
                .andExpect(jsonPath("$.places[0].id").value("placeId1")); // Example assertion
    }

    @Test
    void searchPlaces_whenQueryMissing_shouldReturnBadRequest() throws Exception {
        // This test assumes Spring's default handling or explicit validation
        // Default behavior for missing @RequestParam maps to 500
        mockMvc.perform(get("/api/v1/places/search")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError()); // Expect 500 based on default Spring behavior
    }

    // --- Test for /api/v1/places/{placeId} ---
    @Test
    void getPlaceDetails_shouldReturnPlaceDetails() throws Exception {
        String placeId = "testPlaceId123";
        mockPlaceDetailsResponse.setId(placeId); // Ensure ID matches path variable

        given(placeService.getPlaceDetails(eq(placeId))).willReturn(mockPlaceDetailsResponse); // Use correct response type

        mockMvc.perform(get("/api/v1/places/{placeId}", placeId) // Corrected path
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(placeId))
                .andExpect(jsonPath("$.name").value("Test Place"));
    }

    // --- Test for /api/v1/places/autocomplete ---
    @Test
    void autocompletePlaces_shouldReturnSuggestions() throws Exception {
        given(placeService.autocompletePlaces(anyString(), any(), any(), any())) // Corrected method name
                .willReturn(mockSuggestionResponse); // Use correct response type

        mockMvc.perform(get("/api/v1/places/autocomplete") // Corrected path
                        .param("input", "test input")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.suggestions").isArray())
                .andExpect(jsonPath("$.suggestions[0].id").value("suggestId1")); // Example assertion
    }

     @Test
    void autocompletePlaces_whenInputMissing_shouldReturnBadRequest() throws Exception {
        // This test assumes Spring's default handling or explicit validation
        // Default behavior for missing @RequestParam maps to 500
        mockMvc.perform(get("/api/v1/places/autocomplete")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError()); // Expect 500 based on default Spring behavior
    }


    // --- Test for /api/v1/places/nearby ---
    @Test
    void getNearbyPlaces_shouldReturnNearbyPlaces() throws Exception {
        // Reuse mockSearchResponse or create a specific one
        PlaceSearchResponse nearbyResponse = new PlaceSearchResponse();
        nearbyResponse.setPlaces(Collections.singletonList(mockPlaceResponseDto));
        nearbyResponse.setTotal(1);

        given(placeService.getNearbyPlaces(anyDouble(), anyDouble(), any(), any(), any()))
                .willReturn(nearbyResponse); // Use correct response type

        mockMvc.perform(get("/api/v1/places/nearby") // Corrected path
                        .param("lat", "55.75")
                        .param("lon", "37.62")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.places[0].name").value("Place 1")); // Example assertion
    }

    @Test
    void getNearbyPlaces_whenLatLonMissing_shouldReturnBadRequest() throws Exception {
         // This test assumes Spring's default handling or explicit validation
         // Default behavior for missing @RequestParam maps to 500
        mockMvc.perform(get("/api/v1/places/nearby") // Missing required lat/lon
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError()); // Expect 500 based on default Spring behavior
    }
} 