package ru.putevod.app.auth.service;

public interface DataMigrationService {
    
    /**
     * Мигрирует все данные анонимного пользователя к зарегистрированному пользователю
     */
    void migrateAnonymousUserData(Long anonymousUserId, Integer registeredUserId);
    
    /**
     * Мигрирует путешествия анонимного пользователя
     */
    void migrateTrips(Long anonymousUserId, Integer registeredUserId);
    
    /**
     * Мигрирует todo листы анонимного пользователя
     */
    void migrateTodoLists(Long anonymousUserId, Integer registeredUserId);
} 