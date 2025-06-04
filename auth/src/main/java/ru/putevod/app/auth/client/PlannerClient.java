package ru.putevod.app.auth.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Feign клиент для взаимодействия с planner сервисом
 */
@FeignClient(name = "planner", url = "${app.services.planner.url}")
public interface PlannerClient {

    /**
     * Переносит все данные (путешествия и TODO листы) от анонимного пользователя к зарегистрированному
     *
     * @param anonymousUserId ID анонимного пользователя
     * @param registeredUserId ID зарегистрированного пользователя
     * @param serviceToken сервисный токен для межсервисного взаимодействия
     * @return результат миграции
     */
    @PostMapping("/api/v1/migration/complete")
    ResponseEntity<Map<String, Object>> migrateAllData(
            @RequestParam("anonymousUserId") Long anonymousUserId,
            @RequestParam("registeredUserId") Long registeredUserId,
            @RequestHeader("X-Service-Token") String serviceToken
    );

    /**
     * Переносит путешествия от анонимного пользователя к зарегистрированному
     *
     * @param anonymousUserId ID анонимного пользователя
     * @param registeredUserId ID зарегистрированного пользователя
     * @param serviceToken сервисный токен для межсервисного взаимодействия
     * @return результат переноса путешествий
     */
    @PostMapping("/api/v1/migration/trips/transfer")
    ResponseEntity<Map<String, Object>> transferTripsOwnership(
            @RequestParam("anonymousUserId") Long anonymousUserId,
            @RequestParam("registeredUserId") Long registeredUserId,
            @RequestHeader("X-Service-Token") String serviceToken
    );

    /**
     * Переносит TODO листы от анонимного пользователя к зарегистрированному
     *
     * @param anonymousUserId ID анонимного пользователя
     * @param registeredUserId ID зарегистрированного пользователя
     * @param serviceToken сервисный токен для межсервисного взаимодействия
     * @return результат переноса TODO листов
     */
    @PostMapping("/api/v1/migration/todo-lists/transfer")
    ResponseEntity<Map<String, Object>> transferTodoListsOwnership(
            @RequestParam("anonymousUserId") Long anonymousUserId,
            @RequestParam("registeredUserId") Long registeredUserId,
            @RequestHeader("X-Service-Token") String serviceToken
    );
} 