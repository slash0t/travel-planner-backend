package ru.putevod.app.external.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceClientTest {

    @Mock
    private WebClient webClient;

    private AuthServiceClient authServiceClient;

    private static final String TEST_SERVICE_TOKEN = "test-service-token";
    private static final String TEST_USER_TOKEN = "test-user-token";

    @BeforeEach
    void setUp() {
        authServiceClient = new AuthServiceClient(webClient);
        ReflectionTestUtils.setField(authServiceClient, "serviceToken", TEST_SERVICE_TOKEN);
    }

    @Test
    void validateToken_WhenInvalidToken_ReturnsFalse() {
        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        doReturn(requestBodyUriSpec).when(webClient).post();
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri(anyString());
        doReturn(requestBodySpec).when(requestBodySpec).bodyValue(any());
        doReturn(requestBodySpec).when(requestBodySpec).header(anyString(), anyString());
        doReturn(responseSpec).when(requestBodySpec).retrieve();
        when(responseSpec.bodyToMono(any(Class.class)))
                .thenReturn(Mono.just(Map.of("valid", false)));

        boolean result = authServiceClient.validateToken(TEST_USER_TOKEN);

        assertFalse(result);
        verify(webClient).post();
        verify(requestBodyUriSpec).uri("/auth/validate");
        verify(requestBodySpec).bodyValue(argThat(request -> 
            request.toString().contains("token=" + TEST_USER_TOKEN)
        ));
        verify(requestBodySpec).header("X-Service-Token", TEST_SERVICE_TOKEN);
    }

    @Test
    void validateToken_WhenError_ReturnsFalse() {
        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        doReturn(requestBodyUriSpec).when(webClient).post();
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri(anyString());
        doReturn(requestBodySpec).when(requestBodySpec).bodyValue(any());
        doReturn(requestBodySpec).when(requestBodySpec).header(anyString(), anyString());
        doReturn(responseSpec).when(requestBodySpec).retrieve();
        when(responseSpec.bodyToMono(any(Class.class)))
                .thenReturn(Mono.error(new RuntimeException("API Error")));

        boolean result = authServiceClient.validateToken(TEST_USER_TOKEN);

        assertFalse(result);
        verify(webClient).post();
        verify(requestBodyUriSpec).uri("/auth/validate");
        verify(requestBodySpec).bodyValue(argThat(request -> 
            request.toString().contains("token=" + TEST_USER_TOKEN)
        ));
        verify(requestBodySpec).header("X-Service-Token", TEST_SERVICE_TOKEN);
    }

    @Test
    void getUserInfoFromToken_WhenValidToken_ReturnsUserInfo() {
        Map<String, Object> expectedUserInfo = new HashMap<>();
        expectedUserInfo.put("id", 1);
        expectedUserInfo.put("username", "testuser");
        expectedUserInfo.put("email", "test@example.com");
        expectedUserInfo.put("isAdmin", false);

        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        doReturn(requestBodyUriSpec).when(webClient).post();
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri(anyString());
        doReturn(requestBodySpec).when(requestBodySpec).bodyValue(any());
        doReturn(requestBodySpec).when(requestBodySpec).header(anyString(), anyString());
        doReturn(responseSpec).when(requestBodySpec).retrieve();
        when(responseSpec.bodyToMono(Map.class))
                .thenReturn(Mono.just(expectedUserInfo));

        Map<String, Object> result = authServiceClient.getUserInfoFromToken(TEST_USER_TOKEN);

        assertNotNull(result);
        assertEquals(expectedUserInfo, result);
        verify(webClient).post();
        verify(requestBodyUriSpec).uri("/auth/user-info");
        verify(requestBodySpec).bodyValue(argThat(request -> 
            request.toString().contains("token=" + TEST_USER_TOKEN)
        ));
        verify(requestBodySpec).header("X-Service-Token", TEST_SERVICE_TOKEN);
    }

    @Test
    void getUserInfoFromToken_WhenError_ReturnsEmptyMap() {
        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        doReturn(requestBodyUriSpec).when(webClient).post();
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri(anyString());
        doReturn(requestBodySpec).when(requestBodySpec).bodyValue(any());
        doReturn(requestBodySpec).when(requestBodySpec).header(anyString(), anyString());
        doReturn(responseSpec).when(requestBodySpec).retrieve();
        when(responseSpec.bodyToMono(Map.class))
                .thenReturn(Mono.error(new RuntimeException("API Error")));

        Map<String, Object> result = authServiceClient.getUserInfoFromToken(TEST_USER_TOKEN);

        assertEquals(Collections.emptyMap(), result);
        verify(webClient).post();
        verify(requestBodyUriSpec).uri("/auth/user-info");
        verify(requestBodySpec).bodyValue(argThat(request -> 
            request.toString().contains("token=" + TEST_USER_TOKEN)
        ));
        verify(requestBodySpec).header("X-Service-Token", TEST_SERVICE_TOKEN);
    }

    @Test
    void getUserInfoFromToken_WhenException_ReturnsNull() {
        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        doReturn(requestBodyUriSpec).when(webClient).post();
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri(anyString());
        doReturn(requestBodySpec).when(requestBodySpec).bodyValue(any());
        doReturn(requestBodySpec).when(requestBodySpec).header(anyString(), anyString());
        doReturn(responseSpec).when(requestBodySpec).retrieve();
        when(responseSpec.bodyToMono(Map.class))
                .thenThrow(new RuntimeException("Unexpected Error"));

        Map<String, Object> result = authServiceClient.getUserInfoFromToken(TEST_USER_TOKEN);

        assertNull(result);
        verify(webClient).post();
        verify(requestBodyUriSpec).uri("/auth/user-info");
        verify(requestBodySpec).bodyValue(argThat(request -> 
            request.toString().contains("token=" + TEST_USER_TOKEN)
        ));
        verify(requestBodySpec).header("X-Service-Token", TEST_SERVICE_TOKEN);
    }
} 