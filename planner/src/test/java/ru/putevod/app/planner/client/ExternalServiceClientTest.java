package ru.putevod.app.planner.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import ru.putevod.app.planner.dto.external.PixabayResponseDto;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
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
    @DisplayName("Should return null when city is null")
    void getCityImages_WhenCityIsNull_ReturnsNull() {
        PixabayResponseDto result = externalServiceClient.getCityImages(null);

        assertNull(result);
    }

    @Test
    @DisplayName("Should return null when city is empty")
    void getCityImages_WhenCityIsEmpty_ReturnsNull() {
        PixabayResponseDto result = externalServiceClient.getCityImages("");

        assertNull(result);
    }

    @Test
    @DisplayName("Should successfully get city images")
    void getCityImages_WhenValidCity_ReturnsImages() {
        String city = "Moscow";
        PixabayResponseDto expectedResponse = new PixabayResponseDto();

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(any(), any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(PixabayResponseDto.class)).thenReturn(Mono.just(expectedResponse));

        PixabayResponseDto result = externalServiceClient.getCityImages(city);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(webClient).get();
        verify(requestHeadersUriSpec).uri(any(Function.class));
        verify(requestHeadersSpec).header("X-Service-Token", "test_token");
    }

    @Test
    @DisplayName("Should handle 4xx client error")
    void getCityImages_WhenClientError_ReturnsNull() {
        String city = "Moscow";

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(any(), any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(PixabayResponseDto.class)).thenReturn(Mono.empty());

        PixabayResponseDto result = externalServiceClient.getCityImages(city);

        assertNull(result);
    }

    @Test
    @DisplayName("Should handle 5xx server error")
    void getCityImages_WhenServerError_ReturnsNull() {
        String city = "Moscow";
        WebClientResponseException serverError = WebClientResponseException.create(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                null,
                null,
                null
        );

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(any(), any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(PixabayResponseDto.class)).thenReturn(Mono.error(serverError));

        PixabayResponseDto result = externalServiceClient.getCityImages(city);

        assertNull(result);
    }

    @Test
    @DisplayName("Should handle general exception")
    void getCityImages_WhenGeneralException_ReturnsNull() {
        String city = "Moscow";

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(any(), any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(PixabayResponseDto.class)).thenReturn(Mono.error(new RuntimeException("General error")));

        PixabayResponseDto result = externalServiceClient.getCityImages(city);

        assertNull(result);
    }
} 