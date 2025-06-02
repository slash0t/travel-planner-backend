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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.putevod.app.external.dto.response.PixabayResponse;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PixabayImageServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PixabayImageService service;

    private static final String API_KEY = "test-api-key";
    private static final String API_URL = "https://test.pixabay.com/api/";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "apiKey", API_KEY);
        ReflectionTestUtils.setField(service, "apiUrl", API_URL);
    }

    @Test
    @DisplayName("getCityImages - Null city")
    void getCityImages_NullCity() {
        PixabayResponse response = service.getCityImages(null);

        assertNull(response);
    }

    @Test
    @DisplayName("getCityImages - Empty city")
    void getCityImages_EmptyCity() {
        PixabayResponse response = service.getCityImages("");

        assertNull(response);
    }

    @Test
    @DisplayName("getCityImages - Whitespace city")
    void getCityImages_WhitespaceCity() {
        PixabayResponse response = service.getCityImages("   ");

        assertNull(response);
    }

    @Test
    @DisplayName("getCityImages - Successful response with full image data")
    void getCityImages_SuccessfulResponseWithFullData() {
        PixabayResponse.PixabayImage image = createMockImage(1L, "https://example.com/image1.jpg");
        image.setPageURL("https://example.com/page1");
        image.setType("photo");
        image.setTags("city, architecture, landmark");
        image.setPreviewWidth(150);
        image.setPreviewHeight(100);
        image.setWebformatWidth(640);
        image.setWebformatHeight(480);
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

        when(restTemplate.getForEntity(any(String.class), eq(PixabayResponse.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        PixabayResponse response = service.getCityImages("Moscow");

        assertNotNull(response);
        assertEquals(1, response.getTotal());
        assertEquals(1, response.getTotalHits());
        assertEquals(1, response.getHits().size());

        PixabayResponse.PixabayImage resultImage = response.getHits().get(0);
        assertEquals(1L, resultImage.getId());
        assertEquals("https://example.com/page1", resultImage.getPageURL());
        assertEquals("photo", resultImage.getType());
        assertEquals("city, architecture, landmark", resultImage.getTags());
        assertEquals("https://example.com/image1.jpg", resultImage.getLargeImageUrl());
        assertEquals(150, resultImage.getPreviewWidth());
        assertEquals(100, resultImage.getPreviewHeight());
        assertEquals(640, resultImage.getWebformatWidth());
        assertEquals(480, resultImage.getWebformatHeight());
        assertEquals(1920, resultImage.getImageWidth());
        assertEquals(1080, resultImage.getImageHeight());
        assertEquals(2048, resultImage.getImageSize());
        assertEquals(1000, resultImage.getViews());
        assertEquals(500, resultImage.getDownloads());
        assertEquals(50, resultImage.getCollections());
        assertEquals(100, resultImage.getLikes());
        assertEquals(25, resultImage.getComments());
        assertEquals(123L, resultImage.getUserId());
        assertEquals("testUser", resultImage.getUser());
        assertEquals("https://example.com/user.jpg", resultImage.getUserImageUrl());
    }

    @Test
    @DisplayName("getCityImages - Successful response with multiple images")
    void getCityImages_SuccessfulResponseWithMultipleImages() {
        PixabayResponse.PixabayImage image1 = createMockImage(1L, "https://example.com/image1.jpg");
        PixabayResponse.PixabayImage image2 = createMockImage(2L, "https://example.com/image2.jpg");
        PixabayResponse.PixabayImage image3 = createMockImage(3L, "https://example.com/image3.jpg");

        PixabayResponse mockResponse = new PixabayResponse();
        mockResponse.setTotal(3);
        mockResponse.setTotalHits(3);
        mockResponse.setHits(Arrays.asList(image1, image2, image3));

        when(restTemplate.getForEntity(any(String.class), eq(PixabayResponse.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        PixabayResponse response = service.getCityImages("Moscow");

        assertNotNull(response);
        assertEquals(3, response.getTotal());
        assertEquals(3, response.getTotalHits());
        assertEquals(3, response.getHits().size());
        assertEquals("https://example.com/image1.jpg", response.getHits().get(0).getLargeImageUrl());
        assertEquals("https://example.com/image2.jpg", response.getHits().get(1).getLargeImageUrl());
        assertEquals("https://example.com/image3.jpg", response.getHits().get(2).getLargeImageUrl());
    }

    @Test
    @DisplayName("getCityImages - Empty response")
    void getCityImages_EmptyResponse() {
        PixabayResponse mockResponse = new PixabayResponse();
        mockResponse.setTotal(0);
        mockResponse.setTotalHits(0);
        mockResponse.setHits(Collections.emptyList());

        when(restTemplate.getForEntity(any(String.class), eq(PixabayResponse.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        PixabayResponse response = service.getCityImages("Moscow");

        assertNotNull(response);
        assertEquals(0, response.getTotal());
        assertEquals(0, response.getTotalHits());
        assertTrue(response.getHits().isEmpty());
    }

    @Test
    @DisplayName("getCityImages - API error response")
    void getCityImages_ApiErrorResponse() {
        when(restTemplate.getForEntity(any(String.class), eq(PixabayResponse.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        PixabayResponse response = service.getCityImages("Moscow");

        assertNull(response);
    }

    @Test
    @DisplayName("getCityImages - RestClientException")
    void getCityImages_RestClientException() {
        when(restTemplate.getForEntity(any(String.class), eq(PixabayResponse.class)))
                .thenThrow(new RestClientException("API error"));

        PixabayResponse response = service.getCityImages("Moscow");

        assertNull(response);
    }

    @Test
    @DisplayName("getCityImages - Verifies correct URL construction")
    void getCityImages_VerifiesCorrectUrlConstruction() {
        PixabayResponse mockResponse = new PixabayResponse();
        mockResponse.setTotal(1);
        mockResponse.setTotalHits(1);
        mockResponse.setHits(Collections.singletonList(createMockImage(1L, "https://example.com/image1.jpg")));

        when(restTemplate.getForEntity(any(String.class), eq(PixabayResponse.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        service.getCityImages("Moscow");

        verify(restTemplate).getForEntity(
                eq(API_URL + "?key=" + API_KEY + "&q=Moscow достопримечательность&image_type=photo&orientation=horizontal&order=popular&per_page=3&safesearch=true&lang=ru"),
                eq(PixabayResponse.class)
        );
    }

    private PixabayResponse.PixabayImage createMockImage(Long id, String largeImageUrl) {
        PixabayResponse.PixabayImage image = new PixabayResponse.PixabayImage();
        image.setId(id);
        image.setLargeImageUrl(largeImageUrl);
        image.setWebformatUrl(largeImageUrl.replace("large", "web"));
        image.setPreviewUrl(largeImageUrl.replace("large", "preview"));
        image.setImageWidth(1920);
        image.setImageHeight(1080);
        image.setImageSize(1024);
        image.setViews(1000);
        image.setDownloads(500);
        image.setLikes(100);
        image.setComments(50);
        image.setUser("testUser");
        image.setUserId(123L);
        return image;
    }
} 