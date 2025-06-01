package ru.putevod.app.external.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("handleAuthenticationException - Should return UNAUTHORIZED status with error message")
    void handleAuthenticationException_ShouldReturnUnauthorizedStatus() {
        AuthenticationException ex = mock(AuthenticationException.class);
        when(ex.getMessage()).thenReturn("Invalid token");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleAuthenticationException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid token", response.getBody().get("error"));
    }

    @Test
    @DisplayName("handleHttpClientErrorException - Should return UNAUTHORIZED status for unauthorized error")
    void handleHttpClientErrorException_ShouldReturnUnauthorizedStatusForUnauthorizedError() {
        HttpClientErrorException ex = new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Unauthorized");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleHttpClientErrorException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Требуется авторизация для доступа к ресурсу", response.getBody().get("error"));
    }

    @Test
    @DisplayName("handleHttpClientErrorException - Should return original status for non-unauthorized error")
    void handleHttpClientErrorException_ShouldReturnOriginalStatusForNonUnauthorizedError() {
        HttpClientErrorException ex = new HttpClientErrorException(HttpStatus.NOT_FOUND, "Not Found");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleHttpClientErrorException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("404 Not Found", response.getBody().get("error"));
    }

    @Test
    @DisplayName("handleServiceUnavailableException - Should return SERVICE_UNAVAILABLE status")
    void handleServiceUnavailableException_ShouldReturnServiceUnavailableStatus() {
        ServiceUnavailableException ex = new ServiceUnavailableException("Service is down");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleServiceUnavailableException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Service is down", response.getBody().get("error"));
    }

    @Test
    @DisplayName("handleGenericException - Should return INTERNAL_SERVER_ERROR status with generic message")
    void handleGenericException_ShouldReturnInternalServerErrorStatus() {
        Exception ex = new RuntimeException("Unexpected error");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleGenericException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Произошла внутренняя ошибка сервера. Пожалуйста, попробуйте позже.", response.getBody().get("error"));
    }
} 