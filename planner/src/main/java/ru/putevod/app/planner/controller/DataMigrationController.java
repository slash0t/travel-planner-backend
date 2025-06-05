package ru.putevod.app.planner.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.putevod.app.planner.model.TodoList;
import ru.putevod.app.planner.model.Trip;
import ru.putevod.app.planner.service.DataMigrationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/migration")
@Tag(name = "Миграция данных", description = "API для миграции данных анонимного пользователя")
public class DataMigrationController {

    private final DataMigrationService dataMigrationService;

    @Value("${auth.token}")
    private String serviceToken;

    @Operation(
            summary = "Получить путешествия анонимного пользователя",
            description = "Возвращает все путешествия, созданные анонимным пользователем"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список путешествий получен"),
            @ApiResponse(responseCode = "401", description = "Неверный сервисный токен"),
            @ApiResponse(responseCode = "400", description = "Неверные параметры")
    })
    @GetMapping("/trips/{anonymousUserId}")
    public ResponseEntity<List<Trip>> getAnonymousUserTrips(
            @Parameter(description = "ID анонимного пользователя")
            @PathVariable Long anonymousUserId,
            @RequestHeader(value = "X-Service-Token", required = false) String requestServiceToken) {

        validateServiceToken(requestServiceToken);

        List<Trip> trips = dataMigrationService.getAnonymousUserTrips(anonymousUserId);
        return ResponseEntity.ok(trips);
    }

    @Operation(
            summary = "Получить TODO листы анонимного пользователя",
            description = "Возвращает все TODO листы, созданные анонимным пользователем"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список TODO листов получен"),
            @ApiResponse(responseCode = "401", description = "Неверный сервисный токен"),
            @ApiResponse(responseCode = "400", description = "Неверные параметры")
    })
    @GetMapping("/todo-lists/{anonymousUserId}")
    public ResponseEntity<List<TodoList>> getAnonymousUserTodoLists(
            @Parameter(description = "ID анонимного пользователя")
            @PathVariable Long anonymousUserId,
            @RequestHeader(value = "X-Service-Token", required = false) String requestServiceToken) {

        validateServiceToken(requestServiceToken);

        List<TodoList> todoLists = dataMigrationService.getAnonymousUserTodoLists(anonymousUserId);
        return ResponseEntity.ok(todoLists);
    }

    @Operation(
            summary = "Перенести путешествия к зарегистрированному пользователю",
            description = "Переносит владение всеми путешествиями от анонимного пользователя к зарегистрированному"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Путешествия успешно перенесены"),
            @ApiResponse(responseCode = "401", description = "Неверный сервисный токен"),
            @ApiResponse(responseCode = "400", description = "Неверные параметры")
    })
    @PostMapping("/trips/transfer")
    public ResponseEntity<Map<String, Object>> transferTripsOwnership(
            @RequestParam Long anonymousUserId,
            @RequestParam Long registeredUserId,
            @RequestHeader(value = "X-Service-Token", required = false) String requestServiceToken) {

        validateServiceToken(requestServiceToken);

        int transferredTrips = dataMigrationService.transferTripsOwnership(anonymousUserId, registeredUserId);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Путешествия успешно перенесены");
        response.put("transferredTrips", transferredTrips);
        response.put("anonymousUserId", anonymousUserId);
        response.put("registeredUserId", registeredUserId);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Перенести TODO листы к зарегистрированному пользователю",
            description = "Переносит владение всеми TODO листами от анонимного пользователя к зарегистрированному"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "TODO листы успешно перенесены"),
            @ApiResponse(responseCode = "401", description = "Неверный сервисный токен"),
            @ApiResponse(responseCode = "400", description = "Неверные параметры")
    })
    @PostMapping("/todo-lists/transfer")
    public ResponseEntity<Map<String, Object>> transferTodoListsOwnership(
            @RequestParam Long anonymousUserId,
            @RequestParam Long registeredUserId,
            @RequestHeader(value = "X-Service-Token", required = false) String requestServiceToken) {

        validateServiceToken(requestServiceToken);

        int transferredTodoLists = dataMigrationService.transferTodoListsOwnership(anonymousUserId, registeredUserId);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "TODO листы успешно перенесены");
        response.put("transferredTodoLists", transferredTodoLists);
        response.put("anonymousUserId", anonymousUserId);
        response.put("registeredUserId", registeredUserId);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Полная миграция данных",
            description = "Переносит все данные (путешествия и TODO листы) от анонимного пользователя к зарегистрированному"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Данные успешно мигрированы"),
            @ApiResponse(responseCode = "401", description = "Неверный сервисный токен"),
            @ApiResponse(responseCode = "400", description = "Неверные параметры")
    })
    @PostMapping("/complete")
    public ResponseEntity<Map<String, Object>> migrateAllData(
            @RequestParam Long anonymousUserId,
            @RequestParam Long registeredUserId,
            @RequestHeader(value = "X-Service-Token", required = false) String requestServiceToken) {

        validateServiceToken(requestServiceToken);

        int transferredTrips = dataMigrationService.transferTripsOwnership(anonymousUserId, registeredUserId);
        int transferredTodoLists = dataMigrationService.transferTodoListsOwnership(anonymousUserId, registeredUserId);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Данные успешно мигрированы");
        response.put("transferredTrips", transferredTrips);
        response.put("transferredTodoLists", transferredTodoLists);
        response.put("anonymousUserId", anonymousUserId);
        response.put("registeredUserId", registeredUserId);

        log.info("Миграция данных завершена: {} путешествий и {} TODO листов перенесено от пользователя {} к пользователю {}",
                transferredTrips, transferredTodoLists, anonymousUserId, registeredUserId);

        return ResponseEntity.ok(response);
    }

    private void validateServiceToken(String requestServiceToken) {
        if (requestServiceToken == null || !serviceToken.equals(requestServiceToken)) {
            log.warn("Попытка доступа к миграции с неверным сервисным токеном");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный сервисный токен");
        }
    }
} 