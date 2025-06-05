package ru.putevod.app.auth.service;

import ru.putevod.app.auth.model.AnonymousUser;
import ru.putevod.app.auth.model.User;

import java.util.Map;
import java.util.Optional;

public interface AnonymousUserService {

    /**
     * Создает или получает существующего анонимного пользователя по device ID
     */
    AnonymousUser createOrGetAnonymousUser(String deviceId, String deviceInfo, String ipAddress);

    /**
     * Находит анонимного пользователя по device ID
     */
    Optional<AnonymousUser> findByDeviceId(String deviceId);

    /**
     * Создает анонимный токен для пользователя
     */
    Map<String, Object> createAnonymousToken(String deviceId, String deviceInfo, String ipAddress);

    /**
     * Обновляет активность анонимного пользователя
     */
    void updateLastActivity(String deviceId);

    /**
     * Мигрирует данные анонимного пользователя в обычного пользователя
     */
    void migrateAnonymousUserToRegistered(String deviceId, User registeredUser);

    /**
     * Удаляет неактивных анонимных пользователей
     */
    void cleanupInactiveAnonymousUsers();

    /**
     * Получает количество активных анонимных пользователей
     */
    long getActiveAnonymousUsersCount();
} 