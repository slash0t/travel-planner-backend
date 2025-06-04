package ru.putevod.app.planner.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.putevod.app.planner.config.CurrentUser;
import ru.putevod.app.planner.dto.*;
import ru.putevod.app.planner.service.TodoListService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name = "Todo Lists", description = "API для управления списками задач")
@SecurityRequirement(name = "bearerAuth")
public class TodoListController {

    private final TodoListService todoListService;
    private static final Logger log = LoggerFactory.getLogger(TodoListController.class);

    @PostMapping("/todo-lists")
    @Operation(summary = "Создать новый список задач")
    public ResponseEntity<TodoListDto> createTodoList(
            @CurrentUser Long userId,
            @Valid @RequestBody TodoListDto todoListDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(todoListService.createTodoList(userId, todoListDto));
    }

    @PostMapping("/trips/{tripId}/todo-lists")
    @Operation(summary = "Создать новый список задач для поездки")
    public ResponseEntity<TodoListDto> createTripTodoList(
            @CurrentUser Long userId,
            @PathVariable Long tripId,
            @Valid @RequestBody TodoListDto todoListDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(todoListService.createTripTodoList(userId, tripId, todoListDto));
    }

    @GetMapping("/todo-lists")
    @Operation(summary = "Получить все списки задач пользователя")
    public ResponseEntity<Page<TodoListDto>> getUserTodoLists(
            @CurrentUser Long userId,
            Pageable pageable) {
        log.info("Запрос на получение списков задач для пользователя с ID: {}, pageable: {}", userId, pageable);
        try {
            Page<TodoListDto> result = todoListService.getUserTodoLists(userId, pageable);
            log.info("Найдено {} списков задач для пользователя {}", result.getTotalElements(), userId);

            if (result.hasContent()) {
                log.info("Списки задач для пользователя {}: {}", userId,
                        result.getContent().stream()
                                .map(todo -> String.format("ID:%s, Title:'%s', Trip:%s",
                                        todo.getId(), todo.getTitle(),
                                        todo.getTripId() != null ? "ID:" + todo.getTripId() : "null/deleted"))
                                .collect(java.util.stream.Collectors.toList()));
            } else {
                log.warn("Для пользователя {} не найдено ни одного списка задач", userId);
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Ошибка при получении списков задач для пользователя {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/todo-lists/debug")
    @Operation(summary = "Диагностический эндпоинт для получения всех списков пользователя без фильтрации")
    public ResponseEntity<Page<TodoListDto>> getUserTodoListsDebug(
            @CurrentUser Long userId,
            Pageable pageable) {
        log.info("DEBUG: Запрос на получение всех списков задач для пользователя с ID: {}", userId);
        try {
            Page<TodoListDto> result = todoListService.getUserTodoListsSimple(userId, pageable);
            log.info("DEBUG: Найдено {} списков задач для пользователя {}", result.getTotalElements(), userId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("DEBUG: Ошибка при получении списков задач для пользователя {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/trips/{tripId}/todo-lists")
    @Operation(summary = "Получить все списки задач для поездки")
    public ResponseEntity<List<TodoListDto>> getTripTodoLists(
            @CurrentUser Long userId,
            @PathVariable Long tripId) {
        return ResponseEntity.ok(todoListService.getTripTodoLists(userId, tripId));
    }

    @GetMapping("/todo-lists/{listId}")
    @Operation(summary = "Получить список задач по ID")
    public ResponseEntity<TodoListDto> getTodoListById(
            @CurrentUser Long userId,
            @PathVariable Long listId) {
        return ResponseEntity.ok(todoListService.getTodoListById(userId, listId));
    }

    @PutMapping("/todo-lists/{listId}")
    @Operation(summary = "Обновить список задач")
    public ResponseEntity<TodoListDto> updateTodoList(
            @CurrentUser Long userId,
            @PathVariable Long listId,
            @Valid @RequestBody TodoListDto todoListDto) {
        return ResponseEntity.ok(todoListService.updateTodoList(userId, listId, todoListDto));
    }

    @DeleteMapping("/todo-lists/{listId}")
    @Operation(summary = "Удалить список задач")
    public ResponseEntity<Void> deleteTodoList(
            @CurrentUser Long userId,
            @PathVariable Long listId) {
        todoListService.deleteTodoList(userId, listId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/todo-lists/{listId}/items")
    @Operation(summary = "Добавить элемент в список задач")
    public ResponseEntity<TodoItemDto> addTodoItem(
            @CurrentUser Long userId,
            @PathVariable Long listId,
            @Valid @RequestBody CreateTodoItemDto createTodoItemDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(todoListService.addTodoItem(userId, listId, createTodoItemDto));
    }

    @PutMapping("/todo-lists/{listId}/items/{itemId}")
    @Operation(summary = "Обновить элемент списка задач")
    public ResponseEntity<TodoItemDto> updateTodoItem(
            @CurrentUser Long userId,
            @PathVariable Long listId,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateTodoItemDto updateTodoItemDto) {
        return ResponseEntity.ok(todoListService.updateTodoItem(userId, listId, itemId, updateTodoItemDto));
    }

    @PatchMapping("/todo-lists/{listId}/items/{itemId}/reorder")
    @Operation(summary = "Изменить порядок элемента в списке задач")
    public ResponseEntity<TodoItemDto> reorderTodoItem(
            @CurrentUser Long userId,
            @PathVariable Long listId,
            @PathVariable Long itemId,
            @Valid @RequestBody ReorderTodoItemDto reorderDto) {
        return ResponseEntity.ok(todoListService.reorderTodoItem(userId, listId, itemId, reorderDto.getNewPosition()));
    }

    @PutMapping("/todo-lists/{listId}/items/{itemId}/toggle")
    @Operation(summary = "Переключить статус выполнения задачи")
    public ResponseEntity<TodoItemDto> toggleTodoItemComplete(
            @CurrentUser Long userId,
            @PathVariable Long listId,
            @PathVariable Long itemId) {
        return ResponseEntity.ok(todoListService.toggleTodoItemComplete(userId, listId, itemId));
    }

    @PutMapping("/todo-lists/{listId}/items/toggle-all")
    @Operation(summary = "Переключить статус всех задач в списке")
    public ResponseEntity<Void> toggleAllTodoItemsComplete(
            @CurrentUser Long userId,
            @PathVariable Long listId,
            @RequestParam boolean completed) {
        todoListService.toggleAllTodoItemsComplete(userId, listId, completed);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/todo-lists/{listId}/items/{itemId}")
    @Operation(summary = "Удалить элемент списка задач")
    public ResponseEntity<Void> deleteTodoItem(
            @CurrentUser Long userId,
            @PathVariable Long listId,
            @PathVariable Long itemId) {
        todoListService.deleteTodoItem(userId, listId, itemId);
        return ResponseEntity.noContent().build();
    }
}