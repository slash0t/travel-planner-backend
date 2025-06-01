package ru.putevod.app.planner.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.putevod.app.planner.dto.TodoItemDto;
import ru.putevod.app.planner.dto.TodoListDto;
import ru.putevod.app.planner.service.TodoListService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TodoListControllerTest {

    @Mock
    private TodoListService todoListService;

    @InjectMocks
    private TodoListController todoListController;

    private Long userId;
    private Long tripId;
    private Long listId;
    private Long itemId;
    private TodoListDto mockTodoListDto;
    private TodoItemDto mockTodoItemDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        userId = 1L;
        tripId = 1L;
        listId = 1L;
        itemId = 1L;

        mockTodoListDto = TodoListDto.builder()
                .id(listId)
                .userId(userId)
                .tripId(tripId)
                .title("Test List")
                .description("Test Description")
                .listType("GENERAL")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        mockTodoItemDto = TodoItemDto.builder()
                .id(itemId)
                .listId(listId)
                .content("Test Item")
                .completed(false)
                .orderPosition(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createTodoList_ShouldReturnCreatedList() {
        when(todoListService.createTodoList(eq(userId), any(TodoListDto.class)))
                .thenReturn(mockTodoListDto);

        ResponseEntity<TodoListDto> response = todoListController.createTodoList(userId, mockTodoListDto);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockTodoListDto.getId(), response.getBody().getId());
        assertEquals(mockTodoListDto.getTitle(), response.getBody().getTitle());
        verify(todoListService).createTodoList(eq(userId), any(TodoListDto.class));
    }

    @Test
    void createTripTodoList_ShouldReturnCreatedList() {
        when(todoListService.createTripTodoList(eq(userId), eq(tripId), any(TodoListDto.class)))
                .thenReturn(mockTodoListDto);

        ResponseEntity<TodoListDto> response = todoListController.createTripTodoList(userId, tripId, mockTodoListDto);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockTodoListDto.getId(), response.getBody().getId());
        assertEquals(mockTodoListDto.getTripId(), response.getBody().getTripId());
        verify(todoListService).createTripTodoList(eq(userId), eq(tripId), any(TodoListDto.class));
    }

    @Test
    void getUserTodoLists_ShouldReturnLists() {
        Page<TodoListDto> todoLists = new PageImpl<>(Arrays.asList(mockTodoListDto));
        when(todoListService.getUserTodoLists(eq(userId), any(Pageable.class)))
                .thenReturn(todoLists);

        ResponseEntity<Page<TodoListDto>> response = todoListController.getUserTodoLists(userId, Pageable.unpaged());

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        assertEquals(mockTodoListDto.getId(), response.getBody().getContent().get(0).getId());
        verify(todoListService).getUserTodoLists(eq(userId), any(Pageable.class));
    }

    @Test
    void getTripTodoLists_ShouldReturnLists() {
        List<TodoListDto> todoLists = Arrays.asList(mockTodoListDto);
        when(todoListService.getTripTodoLists(userId, tripId))
                .thenReturn(todoLists);

        ResponseEntity<List<TodoListDto>> response = todoListController.getTripTodoLists(userId, tripId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(mockTodoListDto.getId(), response.getBody().get(0).getId());
        verify(todoListService).getTripTodoLists(userId, tripId);
    }

    @Test
    void getTodoListById_ShouldReturnList() {
        when(todoListService.getTodoListById(userId, listId))
                .thenReturn(mockTodoListDto);

        ResponseEntity<TodoListDto> response = todoListController.getTodoListById(userId, listId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockTodoListDto.getId(), response.getBody().getId());
        verify(todoListService).getTodoListById(userId, listId);
    }

    @Test
    void updateTodoList_ShouldReturnUpdatedList() {
        when(todoListService.updateTodoList(eq(userId), eq(listId), any(TodoListDto.class)))
                .thenReturn(mockTodoListDto);

        ResponseEntity<TodoListDto> response = todoListController.updateTodoList(userId, listId, mockTodoListDto);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockTodoListDto.getId(), response.getBody().getId());
        verify(todoListService).updateTodoList(eq(userId), eq(listId), any(TodoListDto.class));
    }

    @Test
    void deleteTodoList_ShouldReturnNoContent() {
        doNothing().when(todoListService).deleteTodoList(userId, listId);

        ResponseEntity<Void> response = todoListController.deleteTodoList(userId, listId);

        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(todoListService).deleteTodoList(userId, listId);
    }

    @Test
    void addTodoItem_ShouldReturnCreatedItem() {
        when(todoListService.addTodoItem(eq(userId), eq(listId), any(TodoItemDto.class)))
                .thenReturn(mockTodoItemDto);

        ResponseEntity<TodoItemDto> response = todoListController.addTodoItem(userId, listId, mockTodoItemDto);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockTodoItemDto.getId(), response.getBody().getId());
        assertEquals(mockTodoItemDto.getContent(), response.getBody().getContent());
        verify(todoListService).addTodoItem(eq(userId), eq(listId), any(TodoItemDto.class));
    }

    @Test
    void updateTodoItem_ShouldReturnUpdatedItem() {
        when(todoListService.updateTodoItem(eq(userId), eq(listId), eq(itemId), any(TodoItemDto.class)))
                .thenReturn(mockTodoItemDto);

        ResponseEntity<TodoItemDto> response = todoListController.updateTodoItem(userId, listId, itemId, mockTodoItemDto);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockTodoItemDto.getId(), response.getBody().getId());
        verify(todoListService).updateTodoItem(eq(userId), eq(listId), eq(itemId), any(TodoItemDto.class));
    }

    @Test
    void toggleTodoItemComplete_ShouldReturnUpdatedItem() {
        when(todoListService.toggleTodoItemComplete(userId, listId, itemId))
                .thenReturn(mockTodoItemDto);

        ResponseEntity<TodoItemDto> response = todoListController.toggleTodoItemComplete(userId, listId, itemId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockTodoItemDto.getId(), response.getBody().getId());
        verify(todoListService).toggleTodoItemComplete(userId, listId, itemId);
    }

    @Test
    void toggleAllTodoItemsComplete_ShouldReturnNoContent() {
        doNothing().when(todoListService).toggleAllTodoItemsComplete(userId, listId, true);

        ResponseEntity<Void> response = todoListController.toggleAllTodoItemsComplete(userId, listId, true);

        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(todoListService).toggleAllTodoItemsComplete(userId, listId, true);
    }

    @Test
    void deleteTodoItem_ShouldReturnNoContent() {
        doNothing().when(todoListService).deleteTodoItem(userId, listId, itemId);

        ResponseEntity<Void> response = todoListController.deleteTodoItem(userId, listId, itemId);

        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(todoListService).deleteTodoItem(userId, listId, itemId);
    }
} 