package ru.putevod.app.planner.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.putevod.app.planner.dto.CreateTodoItemDto;
import ru.putevod.app.planner.dto.CreateTodoListDto;
import ru.putevod.app.planner.dto.TodoItemDto;
import ru.putevod.app.planner.dto.TodoListDto;
import ru.putevod.app.planner.dto.UpdateTodoItemDto;

import java.util.List;

public interface TodoListService {

    TodoListDto createTodoList(Long userId, TodoListDto todoListDto);

    TodoListDto createTripTodoList(Long userId, Long tripId, TodoListDto todoListDto);

    TodoListDto updateTodoList(Long userId, Long listId, TodoListDto todoListDto);

    TodoListDto getTodoListById(Long userId, Long listId);

    Page<TodoListDto> getUserTodoLists(Long userId, Pageable pageable);

    List<TodoListDto> getTripTodoLists(Long userId, Long tripId);

    void deleteTodoList(Long userId, Long listId);

    TodoItemDto addTodoItem(Long userId, Long listId, CreateTodoItemDto createTodoItemDto);

    TodoItemDto updateTodoItem(Long userId, Long listId, Long itemId, UpdateTodoItemDto updateTodoItemDto);

    TodoItemDto toggleTodoItemComplete(Long userId, Long listId, Long itemId);

    void toggleAllTodoItemsComplete(Long userId, Long listId, boolean completed);

    void deleteTodoItem(Long userId, Long listId, Long itemId);
} 