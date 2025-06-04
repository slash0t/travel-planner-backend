package ru.putevod.app.planner.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceClientTest {

    @Mock
    private WebClient webClient;

    private AuthServiceClient authServiceClient;

    private static final String TEST_SERVICE_TOKEN = "test-service-token";
    private static final String TEST_USER_TOKEN = "test-user-token";
    private static final Long TEST_USER_ID = 1L;

    private record TestAuthUserInfoDto(
            Integer id,
            String email,
            String username,
            String avatarUrl,
            boolean emailVerified,
            boolean isAdmin,
            LocalDateTime createdAt
    ) {
    }

    private record TestTokenValidationRequest(String token) {
    }

    private record TestTokenValidationResponse(boolean valid) {
    }

    @BeforeEach
    void setUp() {
        authServiceClient = new AuthServiceClient(webClient);
        ReflectionTestUtils.setField(authServiceClient, "serviceToken", TEST_SERVICE_TOKEN);
    }

    @Test
    @DisplayName("validateToken - Should return false on error")
    void validateToken_ShouldReturnFalseOnError() {
        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        doReturn(requestBodyUriSpec).when(webClient).post();
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri("/auth/validate");
        doReturn(requestBodySpec).when(requestBodySpec).bodyValue(any());
        doReturn(requestBodySpec).when(requestBodySpec).header("X-Service-Token", TEST_SERVICE_TOKEN);
        doReturn(responseSpec).when(requestBodySpec).retrieve();
        when(responseSpec.bodyToMono(any(Class.class)))
                .thenReturn(Mono.error(new RuntimeException("API Error")));

        boolean result = authServiceClient.validateToken(TEST_USER_TOKEN);

        assertFalse(result);
    }

    @Test
    @DisplayName("getUserInfoFromToken - Should return user info for valid token")
    void getUserInfoFromToken_ShouldReturnUserInfoForValidToken() {
        Map<String, Object> expectedUserInfo = Map.of(
                "userId", TEST_USER_ID,
                "username", "testuser",
                "email", "test@example.com",
                "isAdmin", false,
                "roles", new String[]{"USER"}
        );

        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        doReturn(requestBodyUriSpec).when(webClient).post();
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri("/auth/user-info");
        doReturn(requestBodySpec).when(requestBodySpec).bodyValue(any());
        doReturn(requestBodySpec).when(requestBodySpec).header("X-Service-Token", TEST_SERVICE_TOKEN);
        doReturn(responseSpec).when(requestBodySpec).retrieve();
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(expectedUserInfo));

        Map<String, Object> result = authServiceClient.getUserInfoFromToken(TEST_USER_TOKEN);

        assertNotNull(result);
        assertEquals(expectedUserInfo, result);
        verify(webClient).post();
        verify(requestBodyUriSpec).uri("/auth/user-info");
        verify(requestBodySpec).bodyValue(any());
        verify(requestBodySpec).header("X-Service-Token", TEST_SERVICE_TOKEN);
    }

    @Test
    @DisplayName("getUserInfoFromToken - Should return empty map on error")
    void getUserInfoFromToken_ShouldReturnEmptyMapOnError() {
        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        doReturn(requestBodyUriSpec).when(webClient).post();
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri("/auth/user-info");
        doReturn(requestBodySpec).when(requestBodySpec).bodyValue(any());
        doReturn(requestBodySpec).when(requestBodySpec).header("X-Service-Token", TEST_SERVICE_TOKEN);
        doReturn(responseSpec).when(requestBodySpec).retrieve();
        when(responseSpec.bodyToMono(Map.class))
                .thenReturn(Mono.error(new RuntimeException("API Error")));

        Map<String, Object> result = authServiceClient.getUserInfoFromToken(TEST_USER_TOKEN);

        assertEquals(Collections.emptyMap(), result);
    }
} 