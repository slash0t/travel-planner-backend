package ru.putevod.app.library.exception;

/**
 * Исключение для ошибок взаимодействия с внешними сервисами
 */
public class ExternalServiceException extends RuntimeException {
    
    public ExternalServiceException(String message) {
        super(message);
    }
    
    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
} 