package ru.putevod.app.auth.service;

import ru.putevod.app.auth.dto.AuthResponse;
import ru.putevod.app.auth.dto.RegisterRequest;

import java.util.Map;

public interface AuthService {

    /**
     * Создает ответ авторизации для пользователя
     * 
     * @param email Email пользователя
     * @param ipAddress IP-адрес пользователя
     * @param deviceInfo Информация об устройстве
     * @param deviceId Идентификатор устройства для push-уведомлений (опционально)
     * @return Объект с токенами и информацией о пользователе
     */
    AuthResponse createAuthResponse(String email, String ipAddress, String deviceInfo, String deviceId);

    /**
     * Обновляет токен доступа
     * 
     * @param refreshToken Токен обновления
     * @param ipAddress IP-адрес пользователя
     * @param deviceInfo Информация об устройстве
     * @return Новые токены и информация о пользователе
     */
    AuthResponse refreshToken(String refreshToken, String ipAddress, String deviceInfo);

    /**
     * Выход пользователя из системы
     * 
     * @param refreshToken Токен обновления для инвалидации
     */
    void logout(String refreshToken);
    
    /**
     * Регистрирует нового пользователя в системе
     * 
     * @param registerRequest Данные регистрации пользователя
     * @param ipAddress IP-адрес пользователя
     * @param deviceInfo Информация об устройстве
     * @return ID созданного пользователя
     */
    String registerUser(RegisterRequest registerRequest, String ipAddress, String deviceInfo);
    
    /**
     * Подтверждает email пользователя по токену из письма
     * 
     * @param token Токен подтверждения email
     * @param ipAddress IP-адрес пользователя
     * @param deviceInfo Информация об устройстве
     * @return Объект с токенами и информацией о пользователе
     */
    AuthResponse verifyEmail(String token, String ipAddress, String deviceInfo);
    
    /**
     * Повторно отправляет письмо для подтверждения email
     * 
     * @param email Email пользователя
     */
    void resendVerificationEmail(String email);
    
    /**
     * Отправляет письмо для восстановления пароля
     * 
     * @param email Email пользователя
     */
    void sendPasswordResetEmail(String email);
    
    /**
     * Проверяет код для восстановления пароля
     * 
     * @param email Email пользователя
     * @param code Код восстановления из email
     * @return Токен для сброса пароля
     */
    String verifyPasswordResetCode(String email, String code);
    
    /**
     * Сбрасывает пароль пользователя
     * 
     * @param resetToken Токен для сброса пароля
     * @param newPassword Новый пароль
     */
    void resetPassword(String resetToken, String newPassword);
    
    /**
     * Создает анонимный токен для неавторизованных пользователей
     * 
     * @param deviceId Идентификатор устройства (опционально)
     * @return Объект с анонимным токеном и сроком его действия
     */
    Map<String, Object> createAnonymousToken(String deviceId);
} 