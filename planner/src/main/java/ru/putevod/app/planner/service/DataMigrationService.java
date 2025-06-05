package ru.putevod.app.planner.service;

import ru.putevod.app.planner.model.TodoList;
import ru.putevod.app.planner.model.Trip;

import java.util.List;

/**
 * Сервис для миграции данных анонимного пользователя к зарегистрированному
 */
public interface DataMigrationService {

    /**
     * Получает все путешествия анонимного пользователя
     *
     * @param anonymousUserId ID анонимного пользователя
     * @return список путешествий
     */
    List<Trip> getAnonymousUserTrips(Long anonymousUserId);

    /**
     * Получает все TODO листы анонимного пользователя
     *
     * @param anonymousUserId ID анонимного пользователя
     * @return список TODO листов
     */
    List<TodoList> getAnonymousUserTodoLists(Long anonymousUserId);

    /**
     * Переносит владение путешествием от анонимного пользователя к зарегистрированному
     *
     * @param anonymousUserId  ID анонимного пользователя
     * @param registeredUserId ID зарегистрированного пользователя
     * @return количество перенесенных путешествий
     */
    int transferTripsOwnership(Long anonymousUserId, Long registeredUserId);

    /**
     * Переносит владение TODO листами от анонимного пользователя к зарегистрированному
     *
     * @param anonymousUserId  ID анонимного пользователя
     * @param registeredUserId ID зарегистрированного пользователя
     * @return количество перенесенных TODO листов
     */
    int transferTodoListsOwnership(Long anonymousUserId, Long registeredUserId);
} 