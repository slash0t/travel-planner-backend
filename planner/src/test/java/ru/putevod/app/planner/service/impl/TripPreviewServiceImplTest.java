package ru.putevod.app.planner.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.putevod.app.planner.client.ExternalServiceClient;
import ru.putevod.app.planner.dto.external.PixabayResponseDto;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TripPreviewServiceImplTest {

    @Mock
    private ExternalServiceClient externalServiceClient;

    @InjectMocks
    private TripPreviewServiceImpl tripPreviewService;

    private static final String DEFAULT_PREVIEW_URL = "https://via.placeholder.com/800x600?text=Путешествие";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(tripPreviewService, "defaultPreviewUrl", DEFAULT_PREVIEW_URL);
    }

    @Test
    void generatePreviewForCity_WhenCityIsNull_ReturnsDefaultPreview() {
        String result = tripPreviewService.generatePreviewForCity(null);
        assertEquals(DEFAULT_PREVIEW_URL, result);
    }

    @Test
    void generatePreviewForCity_WhenCityIsEmpty_ReturnsDefaultPreview() {
        String result = tripPreviewService.generatePreviewForCity("");
        assertEquals(DEFAULT_PREVIEW_URL, result);
    }

    @Test
    void generatePreviewForCity_WhenNoImagesFound_ReturnsDefaultPreview() {
        when(externalServiceClient.getCityImages("Moscow")).thenReturn(null);
        String result = tripPreviewService.generatePreviewForCity("Moscow");
        assertEquals(DEFAULT_PREVIEW_URL, result);
    }

    @Test
    void generatePreviewForCity_WhenEmptyHits_ReturnsDefaultPreview() {
        PixabayResponseDto response = new PixabayResponseDto();
        response.setHits(Collections.emptyList());
        when(externalServiceClient.getCityImages("Moscow")).thenReturn(response);
        String result = tripPreviewService.generatePreviewForCity("Moscow");
        assertEquals(DEFAULT_PREVIEW_URL, result);
    }

    @Test
    void generatePreviewForCity_WhenLargeImageUrlExists_ReturnsLargeImageUrl() {
        PixabayResponseDto.PixabayImage image = new PixabayResponseDto.PixabayImage();
        image.setLargeImageUrl("https://example.com/large.jpg");
        PixabayResponseDto response = new PixabayResponseDto();
        response.setHits(Collections.singletonList(image));
        when(externalServiceClient.getCityImages("Moscow")).thenReturn(response);
        String result = tripPreviewService.generatePreviewForCity("Moscow");
        assertEquals("https://example.com/large.jpg", result);
    }

    @Test
    void generatePreviewForCity_WhenWebformatUrlExists_ReturnsWebformatUrl() {
        PixabayResponseDto.PixabayImage image = new PixabayResponseDto.PixabayImage();
        image.setWebformatUrl("https://example.com/web.jpg");
        PixabayResponseDto response = new PixabayResponseDto();
        response.setHits(Collections.singletonList(image));
        when(externalServiceClient.getCityImages("Moscow")).thenReturn(response);
        String result = tripPreviewService.generatePreviewForCity("Moscow");
        assertEquals("https://example.com/web.jpg", result);
    }

    @Test
    void generatePreviewForCity_WhenPreviewUrlExists_ReturnsPreviewUrl() {
        PixabayResponseDto.PixabayImage image = new PixabayResponseDto.PixabayImage();
        image.setPreviewUrl("https://example.com/preview.jpg");
        PixabayResponseDto response = new PixabayResponseDto();
        response.setHits(Collections.singletonList(image));
        when(externalServiceClient.getCityImages("Moscow")).thenReturn(response);
        String result = tripPreviewService.generatePreviewForCity("Moscow");
        assertEquals("https://example.com/preview.jpg", result);
    }

    @Test
    void getDefaultPreviewUrl_ReturnsDefaultUrl() {
        String result = tripPreviewService.getDefaultPreviewUrl();
        assertEquals(DEFAULT_PREVIEW_URL, result);
    }
} 