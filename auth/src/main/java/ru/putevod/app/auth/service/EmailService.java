package ru.putevod.app.auth.service;

import ru.putevod.app.auth.model.User;

/**
 * Сервис для работы с отправкой email и токенами подтверждения
 */
public interface EmailService {

    /**
     * Отправляет email с ссылкой для подтверждения email
     *
     * @param email             Email получателя
     * @param username          Имя пользователя
     * @param verificationToken Токен подтверждения
     */
    void sendVerificationEmail(String email, String username, String verificationToken);

    /**
     * Проверяет токен подтверждения email и возвращает пользователя
     *
     * @param token Токен подтверждения
     * @return Пользователь, которому принадлежит токен
     */
    User verifyEmailToken(String token);

    /**
     * Отправляет email с кодом для сброса пароля
     *
     * @param email     Email получателя
     * @param username  Имя пользователя
     * @param resetCode Код сброса пароля
     */
    void sendPasswordResetEmail(String email, String username, String resetCode);

    /**
     * Сохраняет код сброса пароля
     *
     * @param email     Email пользователя
     * @param resetCode Код сброса пароля
     */
    void storeResetCode(String email, String resetCode);

    /**
     * Проверяет код сброса пароля
     *
     * @param email Email пользователя
     * @param code  Код сброса пароля
     * @return true, если код действителен
     */
    boolean verifyResetCode(String email, String code);

    /**
     * Сохраняет токен сброса пароля
     *
     * @param email      Email пользователя
     * @param resetToken Токен сброса пароля
     */
    void storeResetToken(String email, String resetToken);

    /**
     * Получает email по токену сброса пароля
     *
     * @param resetToken Токен сброса пароля
     * @return Email пользователя или null, если токен недействителен
     */
    String getEmailByResetToken(String resetToken);

    /**
     * Инвалидирует токен сброса пароля
     *
     * @param resetToken Токен сброса пароля
     */
    void invalidateResetToken(String resetToken);
} 