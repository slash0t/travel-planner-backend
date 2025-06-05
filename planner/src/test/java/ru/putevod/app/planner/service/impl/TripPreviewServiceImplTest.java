package ru.putevod.app.planner.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.putevod.app.planner.client.ExternalServiceClient;
import ru.putevod.app.planner.dto.external.UnsplashResponseDto;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TripPreviewServiceImplTest {

    @Mock
    private ExternalServiceClient externalServiceClient;

    @InjectMocks
    private TripPreviewServiceImpl tripPreviewService;

    private static final String DEFAULT_PREVIEW_URL = "https://images.unsplash.com/photo-1469474968028-56623f02e42e?w=800&h=600&fit=crop&crop=center";

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
    void generatePreviewForCity_WhenEmptyResults_ReturnsDefaultPreview() {
        UnsplashResponseDto response = new UnsplashResponseDto();
        response.setResults(Collections.emptyList());
        when(externalServiceClient.getCityImages("Moscow")).thenReturn(response);
        String result = tripPreviewService.generatePreviewForCity("Moscow");
        assertEquals(DEFAULT_PREVIEW_URL, result);
    }

    @Test
    void generatePreviewForCity_WhenRegularUrlExists_ReturnsRegularUrl() {
        UnsplashResponseDto.UnsplashImage.Urls urls = new UnsplashResponseDto.UnsplashImage.Urls();
        urls.setRegular("https://example.com/regular.jpg");

        UnsplashResponseDto.UnsplashImage image = new UnsplashResponseDto.UnsplashImage();
        image.setUrls(urls);

        UnsplashResponseDto response = new UnsplashResponseDto();
        response.setResults(Collections.singletonList(image));
        when(externalServiceClient.getCityImages("Moscow")).thenReturn(response);
        String result = tripPreviewService.generatePreviewForCity("Moscow");
        assertEquals("https://example.com/regular.jpg", result);
    }

    @Test
    void generatePreviewForCity_WhenSmallUrlExists_ReturnsSmallUrl() {
        UnsplashResponseDto.UnsplashImage.Urls urls = new UnsplashResponseDto.UnsplashImage.Urls();
        urls.setSmall("https://example.com/small.jpg");

        UnsplashResponseDto.UnsplashImage image = new UnsplashResponseDto.UnsplashImage();
        image.setUrls(urls);

        UnsplashResponseDto response = new UnsplashResponseDto();
        response.setResults(Collections.singletonList(image));
        when(externalServiceClient.getCityImages("Moscow")).thenReturn(response);
        String result = tripPreviewService.generatePreviewForCity("Moscow");
        assertEquals("https://example.com/small.jpg", result);
    }

    @Test
    void generatePreviewForCity_WhenThumbUrlExists_ReturnsThumbUrl() {
        UnsplashResponseDto.UnsplashImage.Urls urls = new UnsplashResponseDto.UnsplashImage.Urls();
        urls.setThumb("https://example.com/thumb.jpg");

        UnsplashResponseDto.UnsplashImage image = new UnsplashResponseDto.UnsplashImage();
        image.setUrls(urls);

        UnsplashResponseDto response = new UnsplashResponseDto();
        response.setResults(Collections.singletonList(image));
        when(externalServiceClient.getCityImages("Moscow")).thenReturn(response);
        String result = tripPreviewService.generatePreviewForCity("Moscow");
        assertEquals("https://example.com/thumb.jpg", result);
    }

    @Test
    void getDefaultPreviewUrl_ReturnsDefaultUrl() {
        String result = tripPreviewService.getDefaultPreviewUrl();
        assertEquals(DEFAULT_PREVIEW_URL, result);
    }
} 