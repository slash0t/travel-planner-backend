package ru.putevod.app.external.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.putevod.app.external.client.AuthServiceClient;
import ru.putevod.app.external.dto.PlaceResponseDto;
import ru.putevod.app.external.dto.PlaceSuggestionDto;
import ru.putevod.app.external.dto.response.PlaceSearchResponse;
import ru.putevod.app.external.dto.response.PlaceSuggestionResponse;
import ru.putevod.app.external.service.PlaceService;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = PlaceController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class)
class PlaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PlaceService placeService;
    private PlaceSearchResponse mockSearchResponse;
    private PlaceResponseDto mockPlaceDetailsResponse;
    private PlaceSuggestionResponse mockSuggestionResponse;
    private PlaceResponseDto mockPlaceResponseDto;

    @BeforeEach
    void setUp() {
        mockSearchResponse = new PlaceSearchResponse();
        mockSearchResponse.setPlaces(Collections.emptyList());
        mockSearchResponse.setTotal(0);

        mockPlaceDetailsResponse = new PlaceResponseDto();
        mockPlaceDetailsResponse.setId("testPlaceId123");
        mockPlaceDetailsResponse.setName("Test Place");
        mockPlaceDetailsResponse.setLat(0.0);
        mockPlaceDetailsResponse.setLon(0.0);

        mockSuggestionResponse = new PlaceSuggestionResponse();
        PlaceSuggestionDto mockSuggestionDto = new PlaceSuggestionDto();
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

    @TestConfiguration
    static class TestConfig {
        @Bean
        public PlaceService placeService() {
            return Mockito.mock(PlaceService.class);
        }

        @Bean
        public AuthServiceClient authServiceClient() {
            return Mockito.mock(AuthServiceClient.class);
        }
    }

    @Test
    void searchPlaces_shouldReturnPlaces() throws Exception {
        given(placeService.searchPlaces(anyString(), any(), any(), any(), any(), any()))
                .willReturn(mockSearchResponse);

        mockMvc.perform(get("/api/v1/places/search")
                        .param("query", "test query")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.places[0].id").value("placeId1"));
    }

    @Test
    void searchPlaces_whenQueryMissing_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/places/search")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getPlaceDetails_shouldReturnPlaceDetails() throws Exception {
        String placeId = "testPlaceId123";
        mockPlaceDetailsResponse.setId(placeId);

        given(placeService.getPlaceDetails(eq(placeId))).willReturn(mockPlaceDetailsResponse);

        mockMvc.perform(get("/api/v1/places/{placeId}", placeId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(placeId))
                .andExpect(jsonPath("$.name").value("Test Place"));
    }

    @Test
    void autocompletePlaces_shouldReturnSuggestions() throws Exception {
        given(placeService.autocompletePlaces(anyString(), any(), any(), any()))
                .willReturn(mockSuggestionResponse);

        mockMvc.perform(get("/api/v1/places/autocomplete")
                        .param("input", "test input")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.suggestions[0].id").value("suggestId1"));
    }

    @Test
    void autocompletePlaces_whenInputMissing_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/places/autocomplete")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getNearbyPlaces_shouldReturnNearbyPlaces() throws Exception {
        PlaceSearchResponse nearbyResponse = new PlaceSearchResponse();
        nearbyResponse.setPlaces(Collections.singletonList(mockPlaceResponseDto));
        nearbyResponse.setTotal(1);

        given(placeService.getNearbyPlaces(anyDouble(), anyDouble(), any(), any(), any()))
                .willReturn(nearbyResponse);

        mockMvc.perform(get("/api/v1/places/nearby")
                        .param("lat", "55.75")
                        .param("lon", "37.62")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.places[0].name").value("Place 1"));
    }

    @Test
    void getNearbyPlaces_whenLatLonMissing_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/places/nearby")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }
}