package ru.putevod.app.external.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import ru.putevod.app.external.dto.response.UnsplashResponse;
import ru.putevod.app.external.service.ImageService;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageControllerTest {

    @Mock
    private ImageService imageService;

    @InjectMocks
    private ImageController imageController;

    private static final String TEST_CITY = "Moscow";
    private UnsplashResponse testResponse;

    @BeforeEach
    void setUp() {
        testResponse = new UnsplashResponse();
        testResponse.setTotal(1);
        testResponse.setTotalPages(1);

        UnsplashResponse.UnsplashImage testImage = new UnsplashResponse.UnsplashImage();
        testImage.setId("test-image-id");
        testImage.setDescription("Test image description");
        testImage.setAltDescription("Test alt description");

        UnsplashResponse.UnsplashImage.Urls urls = new UnsplashResponse.UnsplashImage.Urls();
        urls.setRaw("https://example.com/raw.jpg");
        urls.setFull("https://example.com/full.jpg");
        urls.setRegular("https://example.com/regular.jpg");
        urls.setSmall("https://example.com/small.jpg");
        urls.setThumb("https://example.com/thumb.jpg");

        testImage.setUrls(urls);
        testResponse.setResults(List.of(testImage));
    }

    @Test
    @DisplayName("getCityImages - Should return images when found")
    void getCityImages_ShouldReturnImagesWhenFound() {
        when(imageService.getCityImages(TEST_CITY)).thenReturn(testResponse);

        ResponseEntity<UnsplashResponse> response = imageController.getCityImages(TEST_CITY);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(testResponse, response.getBody());
        verify(imageService).getCityImages(TEST_CITY);
    }

    @Test
    @DisplayName("getCityImages - Should return 404 when no images found")
    void getCityImages_ShouldReturn404WhenNoImagesFound() {
        when(imageService.getCityImages(TEST_CITY)).thenReturn(null);

        ResponseEntity<UnsplashResponse> response = imageController.getCityImages(TEST_CITY);

        assertNotNull(response);
        assertEquals(404, response.getStatusCode().value());
        assertNull(response.getBody());
        verify(imageService).getCityImages(TEST_CITY);
    }

    @Test
    @DisplayName("getCityImagesForService - Should return images when found")
    void getCityImagesForService_ShouldReturnImagesWhenFound() {
        when(imageService.getCityImages(TEST_CITY)).thenReturn(testResponse);

        ResponseEntity<UnsplashResponse> response = imageController.getCityImagesForService(TEST_CITY);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(testResponse, response.getBody());
        verify(imageService).getCityImages(TEST_CITY);
    }

    @Test
    @DisplayName("getCityImagesForService - Should return 404 when no images found")
    void getCityImagesForService_ShouldReturn404WhenNoImagesFound() {
        when(imageService.getCityImages(TEST_CITY)).thenReturn(null);

        ResponseEntity<UnsplashResponse> response = imageController.getCityImagesForService(TEST_CITY);

        assertNotNull(response);
        assertEquals(404, response.getStatusCode().value());
        assertNull(response.getBody());
        verify(imageService).getCityImages(TEST_CITY);
    }

    @Test
    @DisplayName("getCityImages - Should handle empty response")
    void getCityImages_ShouldHandleEmptyResponse() {
        UnsplashResponse emptyResponse = new UnsplashResponse();
        emptyResponse.setTotal(0);
        emptyResponse.setTotalPages(0);
        emptyResponse.setResults(Collections.emptyList());
        when(imageService.getCityImages(TEST_CITY)).thenReturn(emptyResponse);

        ResponseEntity<UnsplashResponse> response = imageController.getCityImages(TEST_CITY);

        assertNotNull(response);
        assertEquals(404, response.getStatusCode().value());
        assertNull(response.getBody());
        verify(imageService).getCityImages(TEST_CITY);
    }

    @Test
    @DisplayName("getCityImagesForService - Should handle empty response")
    void getCityImagesForService_ShouldHandleEmptyResponse() {
        UnsplashResponse emptyResponse = new UnsplashResponse();
        emptyResponse.setTotal(0);
        emptyResponse.setTotalPages(0);
        emptyResponse.setResults(Collections.emptyList());
        when(imageService.getCityImages(TEST_CITY)).thenReturn(emptyResponse);

        ResponseEntity<UnsplashResponse> response = imageController.getCityImagesForService(TEST_CITY);

        assertNotNull(response);
        assertEquals(404, response.getStatusCode().value());
        assertNull(response.getBody());
        verify(imageService).getCityImages(TEST_CITY);
    }
} 