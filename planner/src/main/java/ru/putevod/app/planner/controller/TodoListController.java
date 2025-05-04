package ru.putevod.app.planner.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.putevod.app.planner.dto.TodoItemDto;
import ru.putevod.app.planner.dto.TodoListDto;
import ru.putevod.app.planner.service.TodoListService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Todo Lists", description = "API для управления списками задач")
public class TodoListController {
    
    private final TodoListService todoListService;
    
    @PostMapping("/api/todos")
    @Operation(summary = "Создать новый список задач")
    public ResponseEntity<TodoListDto> createTodoList(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody TodoListDto todoListDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(todoListService.createTodoList(userId, todoListDto));
    }
    
    @PostMapping("/api/trips/{tripId}/todos")
    @Operation(summary = "Создать новый список задач для поездки")
    public ResponseEntity<TodoListDto> createTripTodoList(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long tripId,
            @RequestBody TodoListDto todoListDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(todoListService.createTripTodoList(userId, tripId, todoListDto));
    }
    
    @GetMapping("/api/todos")
    @Operation(summary = "Получить все списки задач пользователя")
    public ResponseEntity<Page<TodoListDto>> getUserTodoLists(
            @RequestHeader("X-User-Id") Long userId,
            Pageable pageable) {
        return ResponseEntity.ok(todoListService.getUserTodoLists(userId, pageable));
    }
    
    @GetMapping("/api/trips/{tripId}/todos")
    @Operation(summary = "Получить все списки задач для поездки")
    public ResponseEntity<List<TodoListDto>> getTripTodoLists(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long tripId) {
        return ResponseEntity.ok(todoListService.getTripTodoLists(userId, tripId));
    }
    
    @GetMapping("/api/todos/{listId}")
    @Operation(summary = "Получить список задач по ID")
    public ResponseEntity<TodoListDto> getTodoListById(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long listId) {
        return ResponseEntity.ok(todoListService.getTodoListById(userId, listId));
    }
    
    @PutMapping("/api/todos/{listId}")
    @Operation(summary = "Обновить список задач")
    public ResponseEntity<TodoListDto> updateTodoList(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long listId,
            @RequestBody TodoListDto todoListDto) {
        return ResponseEntity.ok(todoListService.updateTodoList(userId, listId, todoListDto));
    }
    
    @DeleteMapping("/api/todos/{listId}")
    @Operation(summary = "Удалить список задач")
    public ResponseEntity<Void> deleteTodoList(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long listId) {
        todoListService.deleteTodoList(userId, listId);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/api/todos/{listId}/items")
    @Operation(summary = "Добавить элемент в список задач")
    public ResponseEntity<TodoItemDto> addTodoItem(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long listId,
            @RequestBody TodoItemDto todoItemDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(todoListService.addTodoItem(userId, listId, todoItemDto));
    }
    
    @PutMapping("/api/todos/{listId}/items/{itemId}")
    @Operation(summary = "Обновить элемент списка задач")
    public ResponseEntity<TodoItemDto> updateTodoItem(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long listId,
            @PathVariable Long itemId,
            @RequestBody TodoItemDto todoItemDto) {
        return ResponseEntity.ok(todoListService.updateTodoItem(userId, listId, itemId, todoItemDto));
    }
    
    @PutMapping("/api/todos/{listId}/items/{itemId}/toggle")
    @Operation(summary = "Переключить статус выполнения задачи")
    public ResponseEntity<TodoItemDto> toggleTodoItemComplete(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long listId,
            @PathVariable Long itemId) {
        return ResponseEntity.ok(todoListService.toggleTodoItemComplete(userId, listId, itemId));
    }
    
    @PutMapping("/api/todos/{listId}/items/toggle-all")
    @Operation(summary = "Переключить статус всех задач в списке")
    public ResponseEntity<Void> toggleAllTodoItemsComplete(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long listId,
            @RequestParam boolean completed) {
        todoListService.toggleAllTodoItemsComplete(userId, listId, completed);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/api/todos/{listId}/items/{itemId}")
    @Operation(summary = "Удалить элемент списка задач")
    public ResponseEntity<Void> deleteTodoItem(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long listId,
            @PathVariable Long itemId) {
        todoListService.deleteTodoItem(userId, listId, itemId);
        return ResponseEntity.noContent().build();
    }
} 