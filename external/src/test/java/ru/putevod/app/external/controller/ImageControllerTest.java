package ru.putevod.app.external.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.putevod.app.external.dto.response.PixabayResponse;
import ru.putevod.app.external.service.ImageService;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageControllerTest {

    @Mock
    private ImageService imageService;

    @InjectMocks
    private ImageController controller;

    @Test
    @DisplayName("getCityImages - Successful response")
    void getCityImages_SuccessfulResponse() {
        String city = "Moscow";
        PixabayResponse.PixabayImage image = new PixabayResponse.PixabayImage();
        image.setId(1L);
        image.setPageURL("https://example.com/page1");
        image.setType("photo");
        image.setTags("city, architecture, landmark");
        image.setPreviewUrl("https://example.com/preview1.jpg");
        image.setPreviewWidth(150);
        image.setPreviewHeight(100);
        image.setWebformatUrl("https://example.com/web1.jpg");
        image.setWebformatWidth(640);
        image.setWebformatHeight(480);
        image.setLargeImageUrl("https://example.com/large1.jpg");
        image.setImageWidth(1920);
        image.setImageHeight(1080);
        image.setImageSize(2048);
        image.setViews(1000);
        image.setDownloads(500);
        image.setCollections(50);
        image.setLikes(100);
        image.setComments(25);
        image.setUserId(123L);
        image.setUser("testUser");
        image.setUserImageUrl("https://example.com/user.jpg");

        PixabayResponse mockResponse = new PixabayResponse();
        mockResponse.setTotal(1);
        mockResponse.setTotalHits(1);
        mockResponse.setHits(Collections.singletonList(image));

        when(imageService.getCityImages(city)).thenReturn(mockResponse);

        ResponseEntity<PixabayResponse> response = controller.getCityImages(city);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotal());
        assertEquals(1, response.getBody().getTotalHits());
        assertEquals(1, response.getBody().getHits().size());
        
        PixabayResponse.PixabayImage resultImage = response.getBody().getHits().get(0);
        assertEquals(1L, resultImage.getId());
        assertEquals("https://example.com/page1", resultImage.getPageURL());
        assertEquals("photo", resultImage.getType());
        assertEquals("city, architecture, landmark", resultImage.getTags());
        assertEquals("https://example.com/large1.jpg", resultImage.getLargeImageUrl());
    }

    @Test
    @DisplayName("getCityImages - Empty response")
    void getCityImages_EmptyResponse() {
        String city = "UnknownCity";
        when(imageService.getCityImages(city)).thenReturn(null);

        ResponseEntity<PixabayResponse> response = controller.getCityImages(city);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    @DisplayName("getCityImages - Empty hits")
    void getCityImages_EmptyHits() {
        String city = "EmptyCity";
        PixabayResponse mockResponse = new PixabayResponse();
        mockResponse.setTotal(0);
        mockResponse.setTotalHits(0);
        mockResponse.setHits(Collections.emptyList());

        when(imageService.getCityImages(city)).thenReturn(mockResponse);

        ResponseEntity<PixabayResponse> response = controller.getCityImages(city);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    @DisplayName("getCityImages - Multiple images")
    void getCityImages_MultipleImages() {
        String city = "Paris";
        PixabayResponse.PixabayImage image1 = new PixabayResponse.PixabayImage();
        image1.setId(1L);
        image1.setLargeImageUrl("https://example.com/paris1.jpg");

        PixabayResponse.PixabayImage image2 = new PixabayResponse.PixabayImage();
        image2.setId(2L);
        image2.setLargeImageUrl("https://example.com/paris2.jpg");

        PixabayResponse mockResponse = new PixabayResponse();
        mockResponse.setTotal(2);
        mockResponse.setTotalHits(2);
        mockResponse.setHits(Arrays.asList(image1, image2));

        when(imageService.getCityImages(city)).thenReturn(mockResponse);

        ResponseEntity<PixabayResponse> response = controller.getCityImages(city);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getTotal());
        assertEquals(2, response.getBody().getTotalHits());
        assertEquals(2, response.getBody().getHits().size());
        assertEquals("https://example.com/paris1.jpg", response.getBody().getHits().get(0).getLargeImageUrl());
        assertEquals("https://example.com/paris2.jpg", response.getBody().getHits().get(1).getLargeImageUrl());
    }
} 