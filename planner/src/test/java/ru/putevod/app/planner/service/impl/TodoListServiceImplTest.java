package ru.putevod.app.planner.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.putevod.app.planner.dto.TodoItemDto;
import ru.putevod.app.planner.dto.TodoListDto;
import ru.putevod.app.planner.mapper.TodoItemMapper;
import ru.putevod.app.planner.mapper.TodoListMapper;
import ru.putevod.app.planner.model.TodoItem;
import ru.putevod.app.planner.model.TodoList;
import ru.putevod.app.planner.model.Trip;
import ru.putevod.app.planner.model.User;
import ru.putevod.app.planner.repository.TodoItemRepository;
import ru.putevod.app.planner.repository.TodoListRepository;
import ru.putevod.app.planner.service.TripService;
import ru.putevod.app.planner.service.UserService;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TodoListServiceImplTest {

    @Mock
    private TodoListRepository todoListRepository;
    @Mock
    private TodoItemRepository todoItemRepository;
    @Mock
    private UserService userService;
    @Mock
    private TripService tripService;
    @Mock
    private TodoListMapper todoListMapper;
    @Mock
    private TodoItemMapper todoItemMapper;

    @InjectMocks
    private TodoListServiceImpl todoListService;

    private User user;
    private Trip trip;
    private TodoList todoList;
    private TodoListDto todoListDto;
    private TodoItem todoItem;
    private TodoItemDto todoItemDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(1L);

        trip = mock(Trip.class);
        trip.setTripId(1L);

        todoList = new TodoList();
        todoList.setListId(1L);
        todoList.setUser(user);

        todoListDto = new TodoListDto();
        todoListDto.setId(1L);

        todoItem = new TodoItem();
        todoItem.setItemId(1L);
        todoItem.setTodoList(todoList);

        todoItemDto = new TodoItemDto();
        todoItemDto.setId(1L);
    }

    @Test
    void createTodoList_Success() {
        doReturn(user).when(userService).getUserEntityById(anyLong());
        doReturn(todoList).when(todoListMapper).toEntity(any());
        doReturn(todoList).when(todoListRepository).save(any());
        doReturn(todoListDto).when(todoListMapper).toDto(any());

        TodoListDto result = todoListService.createTodoList(1L, todoListDto);

        assertNotNull(result);
        verify(todoListRepository).save(any());
    }

    @Test
    void createTripTodoList_Success() {
        doReturn(user).when(userService).getUserEntityById(anyLong());
        doReturn(trip).when(tripService).getTripEntityWithAccessCheck(anyLong(), anyLong());
        doReturn(true).when(tripService).hasAccessToTrip(any(User.class), any(Trip.class), eq("admin"), eq("write"));
        doReturn(todoList).when(todoListMapper).toEntity(any());
        doReturn(todoList).when(todoListRepository).save(any());
        doReturn(todoListDto).when(todoListMapper).toDto(any());

        TodoListDto result = todoListService.createTripTodoList(1L, 1L, todoListDto);

        assertNotNull(result);
        verify(todoListRepository).save(any());
    }

    @Test
    void updateTodoList_Success() {
        doReturn(user).when(userService).getUserEntityById(anyLong());
        doReturn(Optional.of(todoList)).when(todoListRepository).findByUserAndListId(any(), anyLong());
        doReturn(todoList).when(todoListRepository).save(any());
        doReturn(todoListDto).when(todoListMapper).toDto(any());

        TodoListDto result = todoListService.updateTodoList(1L, 1L, todoListDto);

        assertNotNull(result);
        verify(todoListRepository).save(any());
    }

    @Test
    void getTodoListById_Success() {
        doReturn(user).when(userService).getUserEntityById(anyLong());
        doReturn(Optional.of(todoList)).when(todoListRepository).findById(anyLong());
        doReturn(todoListDto).when(todoListMapper).toDto(any());

        TodoListDto result = todoListService.getTodoListById(1L, 1L);

        assertNotNull(result);
    }

    @Test
    void getUserTodoLists_Success() {
        Page<TodoList> todoListPage = new PageImpl<>(List.of(todoList));
        doReturn(user).when(userService).getUserEntityById(anyLong());
        doReturn(todoListPage).when(todoListRepository).findAllActiveByUser(any(), any(Pageable.class));
        doReturn(todoListDto).when(todoListMapper).toDto(any());

        Page<TodoListDto> result = todoListService.getUserTodoLists(1L, mock(Pageable.class));

        assertNotNull(result);
        assertFalse(result.getContent().isEmpty());
    }

    @Test
    void getTripTodoLists_Success() {
        doReturn(user).when(userService).getUserEntityById(anyLong());
        doReturn(trip).when(tripService).getTripEntityWithAccessCheck(anyLong(), anyLong());
        doReturn(true).when(tripService).hasAccessToTrip(any(User.class), any(Trip.class), eq("admin"), eq("read"), eq("write"));
        doReturn(List.of(todoList)).when(trip).getTodoLists();
        doReturn(todoListDto).when(todoListMapper).toDto(any());

        List<TodoListDto> result = todoListService.getTripTodoLists(1L, 1L);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void deleteTodoList_Success() {
        doReturn(user).when(userService).getUserEntityById(anyLong());
        doReturn(Optional.of(todoList)).when(todoListRepository).findById(anyLong());

        todoListService.deleteTodoList(1L, 1L);

        verify(todoListRepository).delete(any());
    }

    @Test
    void addTodoItem_Success() {
        doReturn(user).when(userService).getUserEntityById(anyLong());
        doReturn(Optional.of(todoList)).when(todoListRepository).findById(anyLong());
        doReturn(List.of()).when(todoItemRepository).findByTodoListOrderByOrderPositionAsc(any());
        doReturn(todoItem).when(todoItemMapper).fromDto(any(), any());
        doReturn(todoItem).when(todoItemRepository).save(any());
        doReturn(todoItemDto).when(todoItemMapper).toDto(any());

        TodoItemDto result = todoListService.addTodoItem(1L, 1L, todoItemDto);

        assertNotNull(result);
        verify(todoItemRepository).save(any());
    }

    @Test
    void updateTodoItem_Success() {
        doReturn(user).when(userService).getUserEntityById(anyLong());
        doReturn(Optional.of(todoList)).when(todoListRepository).findById(anyLong());
        doReturn(Optional.of(todoItem)).when(todoItemRepository).findByTodoListAndItemId(any(), anyLong());
        doReturn(todoItem).when(todoItemRepository).save(any());
        doReturn(todoItemDto).when(todoItemMapper).toDto(any());

        TodoItemDto result = todoListService.updateTodoItem(1L, 1L, 1L, todoItemDto);

        assertNotNull(result);
        verify(todoItemRepository).save(any());
    }

    @Test
    void toggleTodoItemComplete_Success() {
        doReturn(user).when(userService).getUserEntityById(anyLong());
        doReturn(Optional.of(todoList)).when(todoListRepository).findById(anyLong());
        doReturn(Optional.of(todoItem)).when(todoItemRepository).findByTodoListAndItemId(any(), anyLong());
        doReturn(todoItemDto).when(todoItemMapper).toDto(any());

        TodoItemDto result = todoListService.toggleTodoItemComplete(1L, 1L, 1L);

        assertNotNull(result);
        verify(todoItemRepository).updateCompletionStatus(any(), anyLong(), anyBoolean());
    }

    @Test
    void toggleAllTodoItemsComplete_Success() {
        doReturn(user).when(userService).getUserEntityById(anyLong());
        doReturn(Optional.of(todoList)).when(todoListRepository).findById(anyLong());

        todoListService.toggleAllTodoItemsComplete(1L, 1L, true);

        verify(todoItemRepository).updateAllCompletionStatus(any(), anyBoolean());
    }

    @Test
    void deleteTodoItem_Success() {
        doReturn(user).when(userService).getUserEntityById(anyLong());
        doReturn(Optional.of(todoList)).when(todoListRepository).findById(anyLong());
        doReturn(Optional.of(todoItem)).when(todoItemRepository).findByTodoListAndItemId(any(), anyLong());

        todoListService.deleteTodoItem(1L, 1L, 1L);

        verify(todoItemRepository).delete(any());
    }
} 