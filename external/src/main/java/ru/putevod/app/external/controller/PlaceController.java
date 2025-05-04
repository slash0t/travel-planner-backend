package ru.putevod.app.external.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.putevod.app.external.dto.PlaceRequestDto;
import ru.putevod.app.external.dto.PlaceResponseDto;
import ru.putevod.app.external.dto.response.PlaceSearchResponse;
import ru.putevod.app.external.dto.response.PlaceSuggestionResponse;
import ru.putevod.app.external.security.CurrentUser;
import ru.putevod.app.external.service.PlaceService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;

    @GetMapping("/search")
    public ResponseEntity<PlaceSearchResponse> searchPlaces(
            @RequestParam String query,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon,
            @RequestParam(required = false, defaultValue = "5000") Integer radius,
            @RequestParam(required = false, defaultValue = "20") Integer limit,
            @RequestParam(required = false) String category,
            @CurrentUser(info = true) Map<String, Object> userInfo) {
        
        return ResponseEntity.ok(placeService.searchPlaces(query, lat, lon, radius, limit, category));
    }
    
    @GetMapping("/{placeId}")
    public ResponseEntity<PlaceResponseDto> getPlaceDetails(
            @PathVariable String placeId,
            @CurrentUser(info = true) Map<String, Object> userInfo) {
        return ResponseEntity.ok(placeService.getPlaceDetails(placeId));
    }
    
    @GetMapping("/autocomplete")
    public ResponseEntity<PlaceSuggestionResponse> autocompletePlaces(
            @RequestParam String input,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon,
            @RequestParam(required = false, defaultValue = "5") Integer limit,
            @CurrentUser(info = true) Map<String, Object> userInfo) {
        
        return ResponseEntity.ok(placeService.autocompletePlaces(input, lat, lon, limit));
    }
    
    @GetMapping("/nearby")
    public ResponseEntity<PlaceSearchResponse> getNearbyPlaces(
            @RequestParam Double lat,
            @RequestParam Double lon,
            @RequestParam(required = false, defaultValue = "1000") Integer radius,
            @RequestParam(required = false, defaultValue = "20") Integer limit,
            @RequestParam(required = false) String categories,
            @CurrentUser(info = true) Map<String, Object> userInfo) {
        
        return ResponseEntity.ok(placeService.getNearbyPlaces(lat, lon, radius, limit, categories));
    }
} 