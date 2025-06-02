package ru.putevod.app.auth.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        when(request.getRequestURI()).thenReturn("/test/endpoint");
    }

    @Test
    void handleResponseStatusException() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found");

        ResponseEntity<ApiError> response = exceptionHandler.handleResponseStatusException(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Resource not found", response.getBody().getMessage());
        assertEquals("ResponseStatusException", response.getBody().getError());
    }

    @Test
    void handleValidationException() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        List<FieldError> fieldErrors = new ArrayList<>();
        fieldErrors.add(new FieldError("object", "field", "error message"));

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        ResponseEntity<ApiError> response = exceptionHandler.handleValidationException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Ошибка валидации данных", response.getBody().getMessage());
        assertEquals("ValidationException", response.getBody().getError());
        assertNotNull(response.getBody().getErrors());
        assertEquals(1, response.getBody().getErrors().size());
    }

    @Test
    void handleMissingParameter() {
        MissingServletRequestParameterException ex = new MissingServletRequestParameterException("param", "String");

        ResponseEntity<ApiError> response = exceptionHandler.handleMissingParameter(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Отсутствует обязательный параметр: param", response.getBody().getMessage());
        assertEquals("MissingParameterException", response.getBody().getError());
    }

    @Test
    void handleMessageNotReadable() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Invalid JSON");

        ResponseEntity<ApiError> response = exceptionHandler.handleMessageNotReadable(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Некорректный формат запроса", response.getBody().getMessage());
        assertEquals("MessageNotReadableException", response.getBody().getError());
    }

    @Test
    void handleTypeMismatch() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("param");
        doReturn(Integer.class).when(ex).getRequiredType();

        ResponseEntity<ApiError> response = exceptionHandler.handleTypeMismatch(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Параметр 'param' имеет неверный тип", response.getBody().getMessage());
        assertEquals("TypeMismatchException", response.getBody().getError());
    }

    @Test
    void handleBadCredentials() {
        BadCredentialsException ex = new BadCredentialsException("Invalid credentials");

        ResponseEntity<ApiError> response = exceptionHandler.handleBadCredentials(ex, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Неверные учетные данные", response.getBody().getMessage());
        assertEquals("BadCredentialsException", response.getBody().getError());
    }

    @Test
    void handleAuthentication() {
        AuthenticationException ex = new BadCredentialsException("Invalid token");

        ResponseEntity<ApiError> response = exceptionHandler.handleAuthentication(ex, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Ошибка аутентификации: Invalid token", response.getBody().getMessage());
        assertEquals("AuthenticationException", response.getBody().getError());
    }

    @Test
    void handleAccessDenied() {
        AccessDeniedException ex = new AccessDeniedException("Access denied");

        ResponseEntity<ApiError> response = exceptionHandler.handleAccessDenied(ex, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Доступ запрещен", response.getBody().getMessage());
        assertEquals("AccessDeniedException", response.getBody().getError());
    }

    @Test
    void handleJwtExceptions_ExpiredJwt() {
        ExpiredJwtException ex = new ExpiredJwtException(null, null, "Token expired");

        ResponseEntity<ApiError> response = exceptionHandler.handleJwtExceptions(ex, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Срок действия токена истек", response.getBody().getMessage());
        assertEquals("ExpiredJwtException", response.getBody().getError());
        assertNotNull(response.getBody().getDetails());
    }

    @Test
    void handleJwtExceptions_MalformedJwt() {
        MalformedJwtException ex = new MalformedJwtException("Invalid token");

        ResponseEntity<ApiError> response = exceptionHandler.handleJwtExceptions(ex, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Неверный формат токена", response.getBody().getMessage());
        assertEquals("MalformedJwtException", response.getBody().getError());
    }

    @Test
    void handleJwtExceptions_SignatureException() {
        SignatureException ex = new SignatureException("Invalid signature");

        ResponseEntity<ApiError> response = exceptionHandler.handleJwtExceptions(ex, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Неверная подпись токена", response.getBody().getMessage());
        assertEquals("SignatureException", response.getBody().getError());
    }

    @Test
    void handleJwtExceptions_UnsupportedJwt() {
        UnsupportedJwtException ex = new UnsupportedJwtException("Unsupported token");

        ResponseEntity<ApiError> response = exceptionHandler.handleJwtExceptions(ex, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Неподдерживаемый токен", response.getBody().getMessage());
        assertEquals("UnsupportedJwtException", response.getBody().getError());
    }

    @Test
    void handleJwtExceptions_IllegalArgument() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid arguments");

        ResponseEntity<ApiError> response = exceptionHandler.handleJwtExceptions(ex, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Недопустимые аргументы JWT", response.getBody().getMessage());
        assertEquals("IllegalArgumentException", response.getBody().getError());
    }

    @Test
    void handleGeneralException() {
        Exception ex = new RuntimeException("Unexpected error");

        ResponseEntity<ApiError> response = exceptionHandler.handleGeneralException(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Произошла внутренняя ошибка сервера", response.getBody().getMessage());
        assertEquals("RuntimeException", response.getBody().getError());
    }
} 