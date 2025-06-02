package ru.putevod.app.planner.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import ru.putevod.app.planner.exception.BadRequestException;
import ru.putevod.app.planner.exception.ResourceNotFoundException;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

        // Common stubs
        when(userService.getUserEntityById(anyLong())).thenReturn(user);
    }

    @Test
    @DisplayName("Should create todo list successfully")
    void createTodoList_Success() {
        when(todoListMapper.toEntity(any())).thenReturn(todoList);
        when(todoListRepository.save(any())).thenReturn(todoList);
        when(todoListMapper.toDto(any())).thenReturn(todoListDto);

        TodoListDto result = todoListService.createTodoList(1L, todoListDto);

        assertNotNull(result);
        verify(todoListRepository).save(any());
    }

    @Test
    @DisplayName("Should create trip todo list successfully")
    void createTripTodoList_Success() {
        when(tripService.getTripEntityWithAccessCheck(anyLong(), anyLong())).thenReturn(trip);
        when(tripService.hasAccessToTrip(any(), any(), eq("admin"), eq("write"))).thenReturn(true);
        when(todoListMapper.toEntity(any())).thenReturn(todoList);
        when(todoListRepository.save(any())).thenReturn(todoList);
        when(todoListMapper.toDto(any())).thenReturn(todoListDto);

        TodoListDto result = todoListService.createTripTodoList(1L, 1L, todoListDto);

        assertNotNull(result);
        verify(todoListRepository).save(any());
    }

    @Test
    @DisplayName("Should throw exception when user has no write access to trip")
    void createTripTodoList_NoWriteAccess_ThrowsException() {
        when(tripService.getTripEntityWithAccessCheck(anyLong(), anyLong())).thenReturn(trip);
        when(tripService.hasAccessToTrip(any(), any(), eq("admin"), eq("write"))).thenReturn(false);

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                todoListService.createTripTodoList(1L, 1L, todoListDto)
        );
        assertEquals("У вас нет прав на создание списков задач в этой поездке", exception.getMessage());
    }

    @Test
    @DisplayName("Should update todo list successfully")
    void updateTodoList_Success() {
        when(todoListRepository.findByUserAndListId(any(), anyLong())).thenReturn(Optional.of(todoList));
        when(todoListRepository.save(any())).thenReturn(todoList);
        when(todoListMapper.toDto(any())).thenReturn(todoListDto);

        TodoListDto result = todoListService.updateTodoList(1L, 1L, todoListDto);

        assertNotNull(result);
        verify(todoListRepository).save(any());
    }

    @Test
    @DisplayName("Should throw exception when todo list not found")
    void updateTodoList_NotFound_ThrowsException() {
        when(todoListRepository.findByUserAndListId(any(), anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                todoListService.updateTodoList(1L, 1L, todoListDto)
        );
        assertEquals("Список задач not found with id: '1'", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when user has no write access to trip todo list")
    void updateTodoList_NoWriteAccess_ThrowsException() {
        todoList.setTrip(trip);
        when(todoListRepository.findByUserAndListId(any(), anyLong())).thenReturn(Optional.of(todoList));
        when(tripService.hasAccessToTrip(any(), any(), eq("admin"), eq("write"))).thenReturn(false);

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                todoListService.updateTodoList(1L, 1L, todoListDto)
        );
        assertEquals("У вас нет прав на редактирование этого списка задач", exception.getMessage());
    }

    @Test
    @DisplayName("Should get todo list by id successfully")
    void getTodoListById_Success() {
        when(todoListRepository.findById(anyLong())).thenReturn(Optional.of(todoList));
        when(todoListMapper.toDto(any())).thenReturn(todoListDto);

        TodoListDto result = todoListService.getTodoListById(1L, 1L);

        assertNotNull(result);
    }

    @Test
    @DisplayName("Should throw exception when todo list not found")
    void getTodoListById_NotFound_ThrowsException() {
        when(todoListRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                todoListService.getTodoListById(1L, 1L)
        );
        assertEquals("Список задач not found with id: '1'", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when user has no access to trip todo list")
    void getTodoListById_NoAccess_ThrowsException() {
        todoList.setTrip(trip);
        when(todoListRepository.findById(anyLong())).thenReturn(Optional.of(todoList));
        when(tripService.hasAccessToTrip(any(), any(), eq("admin"), eq("read"), eq("write"))).thenReturn(false);

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                todoListService.getTodoListById(1L, 1L)
        );
        assertEquals("У вас нет доступа к этому списку задач", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when user has no access to other user's todo list")
    void getTodoListById_OtherUser_ThrowsException() {
        User otherUser = new User();
        otherUser.setUserId(2L);
        todoList.setUser(otherUser);
        when(todoListRepository.findById(anyLong())).thenReturn(Optional.of(todoList));

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                todoListService.getTodoListById(1L, 1L)
        );
        assertEquals("У вас нет доступа к этому списку задач", exception.getMessage());
    }

    @Test
    @DisplayName("Should get user todo lists successfully")
    void getUserTodoLists_Success() {
        Page<TodoList> todoListPage = new PageImpl<>(List.of(todoList));
        when(todoListRepository.findAllActiveByUser(any(), any(Pageable.class))).thenReturn(todoListPage);
        when(todoListMapper.toDto(any())).thenReturn(todoListDto);

        Page<TodoListDto> result = todoListService.getUserTodoLists(1L, mock(Pageable.class));

        assertNotNull(result);
        assertFalse(result.getContent().isEmpty());
    }

    @Test
    @DisplayName("Should get trip todo lists successfully")
    void getTripTodoLists_Success() {
        when(tripService.getTripEntityWithAccessCheck(anyLong(), anyLong())).thenReturn(trip);
        when(tripService.hasAccessToTrip(any(), any(), eq("admin"), eq("read"), eq("write"))).thenReturn(true);
        when(trip.getTodoLists()).thenReturn(List.of(todoList));
        when(todoListMapper.toDto(any())).thenReturn(todoListDto);

        List<TodoListDto> result = todoListService.getTripTodoLists(1L, 1L);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("Should throw exception when user has no access to trip todo lists")
    void getTripTodoLists_NoAccess_ThrowsException() {
        when(tripService.getTripEntityWithAccessCheck(anyLong(), anyLong())).thenReturn(trip);
        when(tripService.hasAccessToTrip(any(), any(), eq("admin"), eq("read"), eq("write"))).thenReturn(false);

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                todoListService.getTripTodoLists(1L, 1L)
        );
        assertEquals("У вас нет прав на просмотр списков задач в этой поездке", exception.getMessage());
    }

    @Test
    @DisplayName("Should delete todo list successfully")
    void deleteTodoList_Success() {
        when(todoListRepository.findById(anyLong())).thenReturn(Optional.of(todoList));

        todoListService.deleteTodoList(1L, 1L);

        verify(todoListRepository).delete(any());
    }

    @Test
    @DisplayName("Should throw exception when todo list not found")
    void deleteTodoList_NotFound_ThrowsException() {
        when(todoListRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                todoListService.deleteTodoList(1L, 1L)
        );
        assertEquals("Список задач not found with id: '1'", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when user has no write access to trip todo list")
    void deleteTodoList_NoWriteAccess_ThrowsException() {
        todoList.setTrip(trip);
        when(todoListRepository.findById(anyLong())).thenReturn(Optional.of(todoList));
        when(tripService.hasAccessToTrip(any(), any(), eq("admin"), eq("write"))).thenReturn(false);

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                todoListService.deleteTodoList(1L, 1L)
        );
        assertEquals("У вас нет прав на удаление этого списка задач", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when user has no access to other user's todo list")
    void deleteTodoList_OtherUser_ThrowsException() {
        User otherUser = new User();
        otherUser.setUserId(2L);
        todoList.setUser(otherUser);
        when(todoListRepository.findById(anyLong())).thenReturn(Optional.of(todoList));

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                todoListService.deleteTodoList(1L, 1L)
        );
        assertEquals("У вас нет прав на удаление этого списка задач", exception.getMessage());
    }

    @Test
    @DisplayName("Should add todo item successfully")
    void addTodoItem_Success() {
        when(todoListRepository.findById(anyLong())).thenReturn(Optional.of(todoList));
        when(todoItemMapper.fromDto(any(), any())).thenReturn(todoItem);
        when(todoItemRepository.save(any())).thenReturn(todoItem);
        when(todoItemMapper.toDto(any())).thenReturn(todoItemDto);

        TodoItemDto result = todoListService.addTodoItem(1L, 1L, todoItemDto);

        assertNotNull(result);
        verify(todoItemRepository).save(any());
    }

    @Test
    @DisplayName("Should throw exception when todo list not found")
    void addTodoItem_ListNotFound_ThrowsException() {
        when(todoListRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                todoListService.addTodoItem(1L, 1L, todoItemDto)
        );
        assertEquals("Список задач not found with id: '1'", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when user has no write access to trip todo list")
    void addTodoItem_NoWriteAccess_ThrowsException() {
        todoList.setTrip(trip);
        when(todoListRepository.findById(anyLong())).thenReturn(Optional.of(todoList));
        when(tripService.hasAccessToTrip(any(), any(), eq("admin"), eq("write"))).thenReturn(false);

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                todoListService.addTodoItem(1L, 1L, todoItemDto)
        );
        assertEquals("У вас нет прав на добавление задач в этот список", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when user has no access to other user's todo list")
    void addTodoItem_OtherUser_ThrowsException() {
        User otherUser = new User();
        otherUser.setUserId(2L);
        todoList.setUser(otherUser);
        when(todoListRepository.findById(anyLong())).thenReturn(Optional.of(todoList));

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                todoListService.addTodoItem(1L, 1L, todoItemDto)
        );
        assertEquals("У вас нет прав на добавление задач в этот список", exception.getMessage());
    }

    @Test
    @DisplayName("Should update todo item successfully")
    void updateTodoItem_Success() {
        when(todoListRepository.findById(anyLong())).thenReturn(Optional.of(todoList));
        when(todoItemRepository.findByTodoListAndItemId(any(), anyLong())).thenReturn(Optional.of(todoItem));
        when(todoItemRepository.save(any())).thenReturn(todoItem);
        when(todoItemMapper.toDto(any())).thenReturn(todoItemDto);

        TodoItemDto result = todoListService.updateTodoItem(1L, 1L, 1L, todoItemDto);

        assertNotNull(result);
        verify(todoItemRepository).save(any());
    }

    @Test
    @DisplayName("Should throw exception when todo item not found")
    void updateTodoItem_ItemNotFound_ThrowsException() {
        when(todoListRepository.findById(anyLong())).thenReturn(Optional.of(todoList));
        when(todoItemRepository.findByTodoListAndItemId(any(), anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                todoListService.updateTodoItem(1L, 1L, 1L, todoItemDto)
        );
        assertEquals("Задача not found with id: '1'", exception.getMessage());
    }

    @Test
    @DisplayName("Should toggle todo item complete successfully")
    void toggleTodoItemComplete_Success() {
        when(todoListRepository.findById(anyLong())).thenReturn(Optional.of(todoList));
        when(todoItemRepository.findByTodoListAndItemId(any(), anyLong())).thenReturn(Optional.of(todoItem));
        when(todoItemMapper.toDto(any())).thenReturn(todoItemDto);

        TodoItemDto result = todoListService.toggleTodoItemComplete(1L, 1L, 1L);

        assertNotNull(result);
        verify(todoItemRepository).updateCompletionStatus(any(), anyLong(), anyBoolean());
    }

    @Test
    @DisplayName("Should throw exception when todo item not found")
    void toggleTodoItemComplete_ItemNotFound_ThrowsException() {
        when(todoListRepository.findById(anyLong())).thenReturn(Optional.of(todoList));
        when(todoItemRepository.findByTodoListAndItemId(any(), anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                todoListService.toggleTodoItemComplete(1L, 1L, 1L)
        );
        assertEquals("Задача not found with id: '1'", exception.getMessage());
    }

    @Test
    @DisplayName("Should toggle all todo items complete successfully")
    void toggleAllTodoItemsComplete_Success() {
        when(todoListRepository.findById(anyLong())).thenReturn(Optional.of(todoList));

        todoListService.toggleAllTodoItemsComplete(1L, 1L, true);

        verify(todoItemRepository).updateAllCompletionStatus(any(), anyBoolean());
    }

    @Test
    @DisplayName("Should throw exception when todo list not found")
    void toggleAllTodoItemsComplete_ListNotFound_ThrowsException() {
        when(todoListRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                todoListService.toggleAllTodoItemsComplete(1L, 1L, true)
        );
        assertEquals("Список задач not found with id: '1'", exception.getMessage());
    }

    @Test
    @DisplayName("Should delete todo item successfully")
    void deleteTodoItem_Success() {
        when(todoListRepository.findById(anyLong())).thenReturn(Optional.of(todoList));
        when(todoItemRepository.findByTodoListAndItemId(any(), anyLong())).thenReturn(Optional.of(todoItem));

        todoListService.deleteTodoItem(1L, 1L, 1L);

        verify(todoItemRepository).delete(any());
    }

    @Test
    @DisplayName("Should throw exception when todo item not found")
    void deleteTodoItem_ItemNotFound_ThrowsException() {
        when(todoListRepository.findById(anyLong())).thenReturn(Optional.of(todoList));
        when(todoItemRepository.findByTodoListAndItemId(any(), anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                todoListService.deleteTodoItem(1L, 1L, 1L)
        );
        assertEquals("Задача not found with id: '1'", exception.getMessage());
    }
} 