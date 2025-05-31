package ru.putevod.app.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import ru.putevod.app.auth.exception.ApiError;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtExceptionHandlerTest {    @Mock
    private HttpServletRequest request;

    @Mock(lenient = true)
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock(lenient = true)
    private ObjectMapper objectMapper;

    @InjectMocks
    private JwtExceptionHandler jwtExceptionHandler;

    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws Exception {
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);
        when(objectMapper.writeValueAsString(any(ApiError.class))).thenReturn("{}");
    }

    @Test
    void doFilterInternal_whenExpiredJwtException_shouldHandleCorrectly() throws Exception {
        doThrow(new ExpiredJwtException(null, null, "Token expired"))
            .when(filterChain).doFilter(request, response);
        when(request.getRequestURI()).thenReturn("/api/test");
        
        jwtExceptionHandler.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(objectMapper).writeValueAsString(any(ApiError.class));
    }

    @Test
    void doFilterInternal_whenSignatureException_shouldHandleCorrectly() throws Exception {
        doThrow(new SignatureException("Invalid signature"))
            .when(filterChain).doFilter(request, response);
        when(request.getRequestURI()).thenReturn("/api/test");

        jwtExceptionHandler.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(objectMapper).writeValueAsString(any(ApiError.class));
    }

    @Test
    void doFilterInternal_whenMalformedJwtException_shouldHandleCorrectly() throws Exception {
        doThrow(new MalformedJwtException("Invalid token format"))
            .when(filterChain).doFilter(request, response);
        when(request.getRequestURI()).thenReturn("/api/test");

        jwtExceptionHandler.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(objectMapper).writeValueAsString(any(ApiError.class));
    }

    @Test
    void doFilterInternal_whenUnsupportedJwtException_shouldHandleCorrectly() throws Exception {
        doThrow(new UnsupportedJwtException("Unsupported token"))
            .when(filterChain).doFilter(request, response);
        when(request.getRequestURI()).thenReturn("/api/test");

        jwtExceptionHandler.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(objectMapper).writeValueAsString(any(ApiError.class));
    }

    @Test
    void doFilterInternal_whenIllegalArgumentException_shouldHandleCorrectly() throws Exception {
        doThrow(new IllegalArgumentException("Invalid argument"))
            .when(filterChain).doFilter(request, response);
        when(request.getRequestURI()).thenReturn("/api/test");

        jwtExceptionHandler.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(objectMapper).writeValueAsString(any(ApiError.class));
    }

    @Test
    void doFilterInternal_whenNoException_shouldProceedWithChain() throws Exception {
        jwtExceptionHandler.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(objectMapper);
    }
}
