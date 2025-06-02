package ru.putevod.app.library.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import ru.putevod.app.library.entity.Trip;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlannerClientTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private PlannerClient plannerClient;

    @BeforeEach
    void setUp() {
        plannerClient = new PlannerClient(webClientBuilder);
        ReflectionTestUtils.setField(plannerClient, "plannerServiceUrl", "http://localhost:8082");

        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.defaultHeader(anyString(), anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
    }

    @Test
    @DisplayName("getRouteDetails - Should return trip details on successful response")
    void getRouteDetails_ShouldReturnTripDetailsOnSuccessfulResponse() {
        Long tripId = 1L;
        String token = "test-token";
        Trip expectedTrip = Trip.builder()
                .id(tripId)
                .title("Test Trip")
                .build();

        WebClient.RequestHeadersUriSpec<?> requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec<?> requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);

        doReturn(requestHeadersUriSpec).when(webClient).get();
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(anyString(), any(Object[].class));
        doReturn(responseSpec).when(requestHeadersSpec).retrieve();
        when(responseSpec.bodyToMono(Trip.class)).thenReturn(Mono.just(expectedTrip));

        Trip result = plannerClient.getRouteDetails(tripId, token);

        assertNotNull(result);
        assertEquals(expectedTrip, result);
        verify(webClientBuilder).baseUrl("http://localhost:8082");
        verify(webClientBuilder).defaultHeader("Content-Type", "application/json");
        verify(webClientBuilder).defaultHeader("Accept", "application/json");
        verify(webClientBuilder).defaultHeader("Authorization", "Bearer " + token);
        verify(webClient).get();
    }

    @Test
    @DisplayName("getRouteDetails - Should return null on error")
    void getRouteDetails_ShouldReturnNullOnError() {
        Long tripId = 1L;
        String token = "test-token";

        WebClient.RequestHeadersUriSpec<?> requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec<?> requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);

        doReturn(requestHeadersUriSpec).when(webClient).get();
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(anyString(), any(Object[].class));
        doReturn(responseSpec).when(requestHeadersSpec).retrieve();
        when(responseSpec.bodyToMono(Trip.class))
                .thenReturn(Mono.<Trip>error(new RuntimeException("API Error"))
                        .onErrorResume(e -> Mono.empty()));

        Trip result = plannerClient.getRouteDetails(tripId, token);

        assertNull(result);
        verify(webClientBuilder).baseUrl("http://localhost:8082");
        verify(webClientBuilder).defaultHeader("Content-Type", "application/json");
        verify(webClientBuilder).defaultHeader("Accept", "application/json");
        verify(webClientBuilder).defaultHeader("Authorization", "Bearer " + token);
        verify(webClient).get();
    }

    @Test
    @DisplayName("canPublishRoute - Should return true on successful response")
    void canPublishRoute_ShouldReturnTrueOnSuccessfulResponse() {
        Long tripId = 1L;
        Long userId = 1L;
        String token = "test-token";

        WebClient.RequestHeadersUriSpec<?> requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec<?> requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);

        doReturn(requestHeadersUriSpec).when(webClient).get();
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(anyString(), any(Object[].class));
        doReturn(responseSpec).when(requestHeadersSpec).retrieve();
        when(responseSpec.bodyToMono(Boolean.class)).thenReturn(Mono.just(true));

        boolean result = plannerClient.canPublishRoute(tripId, userId, token);

        assertTrue(result);
        verify(webClientBuilder).baseUrl("http://localhost:8082");
        verify(webClientBuilder).defaultHeader("Content-Type", "application/json");
        verify(webClientBuilder).defaultHeader("Accept", "application/json");
        verify(webClientBuilder).defaultHeader("Authorization", "Bearer " + token);
        verify(webClient).get();
    }

    @Test
    @DisplayName("canPublishRoute - Should return false on error")
    void canPublishRoute_ShouldReturnFalseOnError() {
        Long tripId = 1L;
        Long userId = 1L;
        String token = "test-token";

        WebClient.RequestHeadersUriSpec<?> requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec<?> requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);

        doReturn(requestHeadersUriSpec).when(webClient).get();
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(anyString(), any(Object[].class));
        doReturn(responseSpec).when(requestHeadersSpec).retrieve();
        when(responseSpec.bodyToMono(Boolean.class))
                .thenReturn(Mono.<Boolean>error(new RuntimeException("API Error"))
                        .onErrorReturn(false));

        boolean result = plannerClient.canPublishRoute(tripId, userId, token);

        assertFalse(result);
        verify(webClientBuilder).baseUrl("http://localhost:8082");
        verify(webClientBuilder).defaultHeader("Content-Type", "application/json");
        verify(webClientBuilder).defaultHeader("Accept", "application/json");
        verify(webClientBuilder).defaultHeader("Authorization", "Bearer " + token);
        verify(webClient).get();
    }

    @Test
    @DisplayName("publishRoute - Should return updated trip on successful response")
    void publishRoute_ShouldReturnUpdatedTripOnSuccessfulResponse() {
        Long tripId = 1L;
        Long userId = 1L;
        String token = "test-token";
        boolean publish = true;
        Trip expectedTrip = Trip.builder()
                .id(tripId)
                .title("Test Trip")
                .isPublic(publish)
                .build();

        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);

        doReturn(requestBodyUriSpec).when(webClient).post();
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri(anyString(), any(Object[].class));
        doReturn(responseSpec).when(requestBodySpec).retrieve();
        when(responseSpec.bodyToMono(Trip.class)).thenReturn(Mono.just(expectedTrip));

        Trip result = plannerClient.publishRoute(tripId, userId, token, publish);

        assertNotNull(result);
        assertEquals(expectedTrip, result);
        verify(webClientBuilder).baseUrl("http://localhost:8082");
        verify(webClientBuilder).defaultHeader("Content-Type", "application/json");
        verify(webClientBuilder).defaultHeader("Accept", "application/json");
        verify(webClientBuilder).defaultHeader("Authorization", "Bearer " + token);
        verify(webClient).post();
    }

    @Test
    @DisplayName("publishRoute - Should return null on error")
    void publishRoute_ShouldReturnNullOnError() {
        Long tripId = 1L;
        Long userId = 1L;
        String token = "test-token";
        boolean publish = true;

        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);

        doReturn(requestBodyUriSpec).when(webClient).post();
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri(anyString(), any(Object[].class));
        doReturn(responseSpec).when(requestBodySpec).retrieve();
        when(responseSpec.bodyToMono(Trip.class))
                .thenReturn(Mono.<Trip>error(new RuntimeException("API Error"))
                        .onErrorResume(e -> Mono.empty()));

        Trip result = plannerClient.publishRoute(tripId, userId, token, publish);

        assertNull(result);
        verify(webClientBuilder).baseUrl("http://localhost:8082");
        verify(webClientBuilder).defaultHeader("Content-Type", "application/json");
        verify(webClientBuilder).defaultHeader("Accept", "application/json");
        verify(webClientBuilder).defaultHeader("Authorization", "Bearer " + token);
        verify(webClient).post();
    }

    @Test
    @DisplayName("getRouteDetails - Should handle WebClientResponseException")
    void getRouteDetails_ShouldHandleWebClientResponseException() {
        Long tripId = 1L;
        String token = "test-token";

        WebClient.RequestHeadersUriSpec<?> requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec<?> requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);

        doReturn(requestHeadersUriSpec).when(webClient).get();
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(anyString(), any(Object[].class));
        doReturn(responseSpec).when(requestHeadersSpec).retrieve();
        when(responseSpec.bodyToMono(Trip.class))
                .thenReturn(Mono.<Trip>error(new WebClientResponseException(401, "Unauthorized", null, null, null))
                        .onErrorResume(e -> Mono.empty()));

        Trip result = plannerClient.getRouteDetails(tripId, token);

        assertNull(result);
        verify(webClientBuilder).baseUrl("http://localhost:8082");
        verify(webClientBuilder).defaultHeader("Content-Type", "application/json");
        verify(webClientBuilder).defaultHeader("Accept", "application/json");
        verify(webClientBuilder).defaultHeader("Authorization", "Bearer " + token);
        verify(webClient).get();
    }

    @Test
    @DisplayName("getRouteDetails - Should handle general Exception")
    void getRouteDetails_ShouldHandleGeneralException() {
        Long tripId = 1L;
        String token = "test-token";

        WebClient.RequestHeadersUriSpec<?> requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec<?> requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);

        doReturn(requestHeadersUriSpec).when(webClient).get();
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(anyString(), any(Object[].class));
        doReturn(responseSpec).when(requestHeadersSpec).retrieve();
        when(responseSpec.bodyToMono(Trip.class))
                .thenReturn(Mono.<Trip>error(new Exception("General error"))
                        .onErrorResume(e -> Mono.empty()));

        Trip result = plannerClient.getRouteDetails(tripId, token);

        assertNull(result);
        verify(webClientBuilder).baseUrl("http://localhost:8082");
        verify(webClientBuilder).defaultHeader("Content-Type", "application/json");
        verify(webClientBuilder).defaultHeader("Accept", "application/json");
        verify(webClientBuilder).defaultHeader("Authorization", "Bearer " + token);
        verify(webClient).get();
    }
} 