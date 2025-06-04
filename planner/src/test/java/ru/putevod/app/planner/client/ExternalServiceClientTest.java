package ru.putevod.app.planner.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import ru.putevod.app.planner.dto.external.UnsplashResponseDto;

import java.util.Collections;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExternalServiceClientTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private ExternalServiceClient externalServiceClient;

    @BeforeEach
    void setUp() {
        externalServiceClient = new ExternalServiceClient(webClient);
        ReflectionTestUtils.setField(externalServiceClient, "serviceToken", "test_token");
    }

    @Test
    void getCityImages_WhenCityIsNull_ReturnsNull() {
        UnsplashResponseDto result = externalServiceClient.getCityImages(null);
        assertNull(result);
    }

    @Test
    void getCityImages_WhenCityIsEmpty_ReturnsNull() {
        UnsplashResponseDto result = externalServiceClient.getCityImages("");
        assertNull(result);
    }

    @Test
    void getCityImages_WhenSuccessfulResponse_ReturnsUnsplashResponseDto() {
        String city = "Moscow";

        UnsplashResponseDto.UnsplashImage.Urls urls = new UnsplashResponseDto.UnsplashImage.Urls();
        urls.setRegular("https://example.com/regular.jpg");
        urls.setSmall("https://example.com/small.jpg");
        urls.setThumb("https://example.com/thumb.jpg");

        UnsplashResponseDto.UnsplashImage image = new UnsplashResponseDto.UnsplashImage();
        image.setId("test-id");
        image.setUrls(urls);

        UnsplashResponseDto expectedResponse = new UnsplashResponseDto();
        expectedResponse.setTotal(1);
        expectedResponse.setResults(Collections.singletonList(image));

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(any(), any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(UnsplashResponseDto.class)).thenReturn(Mono.just(expectedResponse));

        UnsplashResponseDto result = externalServiceClient.getCityImages(city);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertNotNull(result.getResults());
        assertEquals(1, result.getResults().size());
        assertEquals("test-id", result.getResults().get(0).getId());
        assertEquals("https://example.com/regular.jpg", result.getResults().get(0).getUrls().getRegular());
    }

    @Test
    void getCityImages_WhenServerError_ReturnsNull() {
        String city = "Moscow";

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(any(), any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(UnsplashResponseDto.class)).thenReturn(Mono.error(
                WebClientResponseException.create(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Internal Server Error", null, null, null)));

        UnsplashResponseDto result = externalServiceClient.getCityImages(city);

        assertNull(result);
    }

    @Test
    void getCityImages_WhenClientError_ReturnsNull() {
        String city = "Moscow";

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(any(), any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(UnsplashResponseDto.class)).thenReturn(Mono.empty());

        UnsplashResponseDto result = externalServiceClient.getCityImages(city);

        assertNull(result);
    }

    @Test
    void getCityImages_WhenGeneralException_ReturnsNull() {
        String city = "Moscow";

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(any(), any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(UnsplashResponseDto.class)).thenReturn(Mono.error(new RuntimeException("General error")));

        UnsplashResponseDto result = externalServiceClient.getCityImages(city);

        assertNull(result);
    }
}