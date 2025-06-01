package ru.putevod.app.library.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.putevod.app.library.client.AuthServiceClient.UserInfo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceClientTest {

    @Mock
    private WebClient webClient;

    private AuthServiceClient authServiceClient;

    @BeforeEach
    void setUp() {
        authServiceClient = new AuthServiceClient(webClient);
        ReflectionTestUtils.setField(authServiceClient, "serviceToken", "test-service-token");
    }

    @Test
    @DisplayName("validateUserToken - Should return true for valid token")
    void validateUserToken_ShouldReturnTrueForValidToken() {
        String token = "valid-token";
        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        doReturn(requestBodyUriSpec).when(webClient).post();
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri(anyString());
        doReturn(requestBodySpec).when(requestBodySpec).bodyValue(any());
        doReturn(requestBodySpec).when(requestBodySpec).header(anyString(), anyString());
        doReturn(responseSpec).when(requestBodySpec).retrieve();
        when(responseSpec.bodyToMono(AuthServiceClient.TokenValidationResponse.class))
                .thenReturn(Mono.just(new AuthServiceClient.TokenValidationResponse(true)));

        boolean result = authServiceClient.validateUserToken(token);

        assertTrue(result);
        verify(webClient).post();
        verify(requestBodyUriSpec).uri("/auth/validate");
        verify(requestBodySpec).bodyValue(any(AuthServiceClient.TokenValidationRequest.class));
        verify(requestBodySpec).header("X-Service-Token", "test-service-token");
    }

    @Test
    @DisplayName("validateUserToken - Should return false for invalid token")
    void validateUserToken_ShouldReturnFalseForInvalidToken() {
        String token = "invalid-token";
        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        doReturn(requestBodyUriSpec).when(webClient).post();
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri(anyString());
        doReturn(requestBodySpec).when(requestBodySpec).bodyValue(any());
        doReturn(requestBodySpec).when(requestBodySpec).header(anyString(), anyString());
        doReturn(responseSpec).when(requestBodySpec).retrieve();
        when(responseSpec.bodyToMono(AuthServiceClient.TokenValidationResponse.class))
                .thenReturn(Mono.just(new AuthServiceClient.TokenValidationResponse(false)));

        boolean result = authServiceClient.validateUserToken(token);

        assertFalse(result);
        verify(webClient).post();
        verify(requestBodyUriSpec).uri("/auth/validate");
        verify(requestBodySpec).bodyValue(any(AuthServiceClient.TokenValidationRequest.class));
        verify(requestBodySpec).header("X-Service-Token", "test-service-token");
    }

    @Test
    @DisplayName("validateUserToken - Should return false on error")
    void validateUserToken_ShouldReturnFalseOnError() {
        String token = "error-token";
        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        doReturn(requestBodyUriSpec).when(webClient).post();
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri(anyString());
        doReturn(requestBodySpec).when(requestBodySpec).bodyValue(any());
        doReturn(requestBodySpec).when(requestBodySpec).header(anyString(), anyString());
        doReturn(responseSpec).when(requestBodySpec).retrieve();
        when(responseSpec.bodyToMono(AuthServiceClient.TokenValidationResponse.class))
                .thenReturn(Mono.error(new RuntimeException("API Error")));

        boolean result = authServiceClient.validateUserToken(token);

        assertFalse(result);
        verify(webClient).post();
        verify(requestBodyUriSpec).uri("/auth/validate");
        verify(requestBodySpec).bodyValue(any(AuthServiceClient.TokenValidationRequest.class));
        verify(requestBodySpec).header("X-Service-Token", "test-service-token");
    }

    @Test
    @DisplayName("getUserInfo - Should return user info for valid token")
    void getUserInfo_ShouldReturnUserInfoForValidToken() {
        String token = "valid-token";
        UserInfo expectedUserInfo = new UserInfo(1L, "testuser", "test@example.com", false, new String[]{"USER"});

        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        doReturn(requestBodyUriSpec).when(webClient).post();
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri(anyString());
        doReturn(requestBodySpec).when(requestBodySpec).bodyValue(any());
        doReturn(requestBodySpec).when(requestBodySpec).header(anyString(), anyString());
        doReturn(responseSpec).when(requestBodySpec).retrieve();
        when(responseSpec.bodyToMono(UserInfo.class))
                .thenReturn(Mono.just(expectedUserInfo));

        UserInfo result = authServiceClient.getUserInfo(token);

        assertNotNull(result);
        assertEquals(expectedUserInfo, result);
        verify(webClient).post();
        verify(requestBodyUriSpec).uri("/auth/userinfo");
        verify(requestBodySpec).bodyValue(any(AuthServiceClient.TokenValidationRequest.class));
        verify(requestBodySpec).header("X-Service-Token", "test-service-token");
    }

    @Test
    @DisplayName("getUserInfo - Should return null on error")
    void getUserInfo_ShouldReturnNullOnError() {
        String token = "error-token";
        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        doReturn(requestBodyUriSpec).when(webClient).post();
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri(anyString());
        doReturn(requestBodySpec).when(requestBodySpec).bodyValue(any());
        doReturn(requestBodySpec).when(requestBodySpec).header(anyString(), anyString());
        doReturn(responseSpec).when(requestBodySpec).retrieve();
        when(responseSpec.bodyToMono(UserInfo.class))
                .thenReturn(Mono.error(new RuntimeException("API Error")));

        UserInfo result = authServiceClient.getUserInfo(token);

        assertNull(result);
        verify(webClient).post();
        verify(requestBodyUriSpec).uri("/auth/userinfo");
        verify(requestBodySpec).bodyValue(any(AuthServiceClient.TokenValidationRequest.class));
        verify(requestBodySpec).header("X-Service-Token", "test-service-token");
    }
} 