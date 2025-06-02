package ru.putevod.app.auth.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleResponseStatusException(
            ResponseStatusException ex,
            HttpServletRequest request) {

        ApiError apiError = createApiError(
                ex.getStatusCode().value(),
                ex.getReason(),
                request.getRequestURI(),
                "ResponseStatusException"
        );

        return new ResponseEntity<>(apiError, ex.getStatusCode());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        BindingResult result = ex.getBindingResult();
        List<Map<String, String>> errors = new ArrayList<>();

        for (FieldError error : result.getFieldErrors()) {
            Map<String, String> fieldError = new HashMap<>();
            fieldError.put("field", error.getField());
            fieldError.put("message", error.getDefaultMessage());
            errors.add(fieldError);
        }

        ApiError apiError = createApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Ошибка валидации данных",
                request.getRequestURI(),
                "ValidationException"
        );
        apiError.setErrors(errors);

        log.warn("Validation error: {}", errors);
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParameter(
            MissingServletRequestParameterException ex,
            HttpServletRequest request) {

        ApiError apiError = createApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Отсутствует обязательный параметр: " + ex.getParameterName(),
                request.getRequestURI(),
                "MissingParameterException"
        );

        log.warn("Missing parameter: {}", ex.getParameterName());
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        ApiError apiError = createApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Некорректный формат запроса",
                request.getRequestURI(),
                "MessageNotReadableException"
        );

        log.warn("Message not readable: {}", ex.getMessage());
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        ApiError apiError = createApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Параметр '" + ex.getName() + "' имеет неверный тип",
                request.getRequestURI(),
                "TypeMismatchException"
        );

        log.warn("Type mismatch: {} - Expected: {}", ex.getName(), ex.getRequiredType().getSimpleName());
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(
            BadCredentialsException ex,
            HttpServletRequest request) {

        ApiError apiError = createApiError(
                HttpStatus.UNAUTHORIZED.value(),
                "Неверные учетные данные",
                request.getRequestURI(),
                "BadCredentialsException"
        );

        log.warn("Bad credentials: {}", ex.getMessage());
        return new ResponseEntity<>(apiError, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthentication(
            AuthenticationException ex,
            HttpServletRequest request) {

        ApiError apiError = createApiError(
                HttpStatus.UNAUTHORIZED.value(),
                "Ошибка аутентификации: " + ex.getMessage(),
                request.getRequestURI(),
                "AuthenticationException"
        );

        log.warn("Authentication error: {}", ex.getMessage());
        return new ResponseEntity<>(apiError, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {

        ApiError apiError = createApiError(
                HttpStatus.FORBIDDEN.value(),
                "Доступ запрещен",
                request.getRequestURI(),
                "AccessDeniedException"
        );

        log.warn("Access denied: {}", ex.getMessage());
        return new ResponseEntity<>(apiError, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({
            ExpiredJwtException.class,
            MalformedJwtException.class,
            SignatureException.class,
            UnsupportedJwtException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ApiError> handleJwtExceptions(
            Exception ex,
            HttpServletRequest request) {

        String message = "Ошибка JWT токена";
        String errorType = "JwtException";

        if (ex instanceof ExpiredJwtException) {
            message = "Срок действия токена истек";
            errorType = "ExpiredJwtException";
        } else if (ex instanceof MalformedJwtException) {
            message = "Неверный формат токена";
            errorType = "MalformedJwtException";
        } else if (ex instanceof SignatureException) {
            message = "Неверная подпись токена";
            errorType = "SignatureException";
        } else if (ex instanceof UnsupportedJwtException) {
            message = "Неподдерживаемый токен";
            errorType = "UnsupportedJwtException";
        } else if (ex instanceof IllegalArgumentException) {
            message = "Недопустимые аргументы JWT";
            errorType = "IllegalArgumentException";
        }

        ApiError apiError = createApiError(
                HttpStatus.UNAUTHORIZED.value(),
                message,
                request.getRequestURI(),
                errorType
        );

        Map<String, Object> details = new HashMap<>();
        details.put("errorMessage", ex.getMessage());
        apiError.setDetails(details);

        log.warn("JWT token error: {}", ex.getMessage());
        return new ResponseEntity<>(apiError, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneralException(
            Exception ex,
            HttpServletRequest request) {

        ApiError apiError = createApiError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Произошла внутренняя ошибка сервера",
                request.getRequestURI(),
                ex.getClass().getSimpleName()
        );

        log.error("Unexpected error: ", ex);
        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ApiError createApiError(int status, String message, String path, String errorType) {
        return ApiError.builder()
                .status(status)
                .message(message)
                .path(path)
                .error(errorType)
                .timestamp(LocalDateTime.now())
                .build();
    }
} 