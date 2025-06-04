package ru.putevod.app.external.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.putevod.app.external.dto.response.UnsplashResponse;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UnsplashImageServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private UnsplashImageService unsplashImageService;

    private static final String TEST_ACCESS_KEY = "test-access-key";
    private static final String TEST_API_URL = "https://api.unsplash.com";
    private static final String TEST_CITY = "Moscow";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(unsplashImageService, "accessKey", TEST_ACCESS_KEY);
        ReflectionTestUtils.setField(unsplashImageService, "apiUrl", TEST_API_URL);
    }

    @Test
    void getCityImages_WhenCityIsNull_ReturnsNull() {
        UnsplashResponse result = unsplashImageService.getCityImages(null);
        assertNull(result);
    }

    @Test
    void getCityImages_WhenCityIsEmpty_ReturnsNull() {
        UnsplashResponse result = unsplashImageService.getCityImages("");
        assertNull(result);
    }

    @Test
    void getCityImages_WhenSuccessfulResponse_ReturnsUnsplashResponse() {
        UnsplashResponse.UnsplashImage.Urls urls = new UnsplashResponse.UnsplashImage.Urls();
        urls.setRegular("https://example.com/regular.jpg");
        urls.setSmall("https://example.com/small.jpg");
        urls.setThumb("https://example.com/thumb.jpg");

        UnsplashResponse.UnsplashImage image = new UnsplashResponse.UnsplashImage();
        image.setId("test-id");
        image.setUrls(urls);

        UnsplashResponse expectedResponse = new UnsplashResponse();
        expectedResponse.setTotal(1);
        expectedResponse.setResults(Collections.singletonList(image));

        ResponseEntity<UnsplashResponse> responseEntity = new ResponseEntity<>(expectedResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(UnsplashResponse.class)
        )).thenReturn(responseEntity);

        UnsplashResponse result = unsplashImageService.getCityImages(TEST_CITY);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertNotNull(result.getResults());
        assertEquals(1, result.getResults().size());
        assertEquals("test-id", result.getResults().get(0).getId());
        assertEquals("https://example.com/regular.jpg", result.getResults().get(0).getUrls().getRegular());

        verify(restTemplate).exchange(
                contains("/search/photos"),
                eq(HttpMethod.GET),
                argThat(entity -> {
                    HttpHeaders headers = entity.getHeaders();
                    return headers.getFirst("Authorization").equals("Client-ID " + TEST_ACCESS_KEY) &&
                           headers.getFirst("Accept-Version").equals("v1");
                }),
                eq(UnsplashResponse.class)
        );
    }

    @Test
    void getCityImages_WhenNon2xxResponse_ReturnsNull() {
        ResponseEntity<UnsplashResponse> responseEntity = new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(UnsplashResponse.class)
        )).thenReturn(responseEntity);

        UnsplashResponse result = unsplashImageService.getCityImages(TEST_CITY);

        assertNull(result);
        verify(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(UnsplashResponse.class)
        );
    }

    @Test
    void getCityImages_WhenRestClientException_ReturnsNull() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(UnsplashResponse.class)
        )).thenThrow(new RestClientException("API Error"));

        UnsplashResponse result = unsplashImageService.getCityImages(TEST_CITY);

        assertNull(result);
        verify(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(UnsplashResponse.class)
        );
    }

    @Test
    void getCityImages_WhenUnexpectedException_ReturnsNull() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(UnsplashResponse.class)
        )).thenThrow(new RuntimeException("Unexpected Error"));

        UnsplashResponse result = unsplashImageService.getCityImages(TEST_CITY);

        assertNull(result);
        verify(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(UnsplashResponse.class)
        );
    }

    @Test
    void getCityImages_WhenResponseBodyIsNull_ReturnsNull() {
        ResponseEntity<UnsplashResponse> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(UnsplashResponse.class)
        )).thenReturn(responseEntity);

        UnsplashResponse result = unsplashImageService.getCityImages(TEST_CITY);

        assertNull(result);
        verify(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(UnsplashResponse.class)
        );
    }
} 