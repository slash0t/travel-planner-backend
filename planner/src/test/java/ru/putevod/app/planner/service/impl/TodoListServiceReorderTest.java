package ru.putevod.app.planner.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import ru.putevod.app.planner.dto.CreateTodoItemDto;
import ru.putevod.app.planner.dto.TodoItemDto;
import ru.putevod.app.planner.exception.BadRequestException;
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

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TodoListServiceReorderTest {

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
    private TodoItem item1, item2, item3, item4;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(1L);

        trip = new Trip();
        trip.setTripId(1L);

        todoList = new TodoList();
        todoList.setListId(1L);
        todoList.setUser(user);

        item1 = TodoItem.builder()
                .itemId(1L)
                .todoList(todoList)
                .content("Item 1")
                .orderPosition(1)
                .completed(false)
                .build();

        item2 = TodoItem.builder()
                .itemId(2L)
                .todoList(todoList)
                .content("Item 2")
                .orderPosition(2)
                .completed(false)
                .build();

        item3 = TodoItem.builder()
                .itemId(3L)
                .todoList(todoList)
                .content("Item 3")
                .orderPosition(3)
                .completed(false)
                .build();

        item4 = TodoItem.builder()
                .itemId(4L)
                .todoList(todoList)
                .content("Item 4")
                .orderPosition(4)
                .completed(false)
                .build();
    }

    @Test
    void reorderTodoItem_Success_MoveUp() {
        when(userService.getUserEntityById(anyLong())).thenReturn(user);
        when(todoListRepository.findById(1L)).thenReturn(Optional.of(todoList));
        when(todoItemRepository.findByTodoListAndItemId(todoList, 4L)).thenReturn(Optional.of(item4));
        when(todoItemRepository.findMaxOrderPositionByTodoList(todoList)).thenReturn(4);
        when(todoItemRepository.findByTodoListAndOrderPositionBetween(todoList, 2, 3))
                .thenReturn(Arrays.asList(item2, item3));
        when(todoItemRepository.save(any())).thenReturn(item4);
        when(todoItemMapper.toDto(any())).thenReturn(new TodoItemDto());

        TodoItemDto result = todoListService.reorderTodoItem(1L, 1L, 4L, 2);

        assertNotNull(result);
        assertEquals(2, item4.getOrderPosition());
        assertEquals(3, item2.getOrderPosition());
        assertEquals(4, item3.getOrderPosition());
        verify(todoItemRepository).saveAll(any());
    }

    @Test
    void reorderTodoItem_Success_MoveDown() {
        when(userService.getUserEntityById(anyLong())).thenReturn(user);
        when(todoListRepository.findById(1L)).thenReturn(Optional.of(todoList));
        when(todoItemRepository.findByTodoListAndItemId(todoList, 1L)).thenReturn(Optional.of(item1));
        when(todoItemRepository.findMaxOrderPositionByTodoList(todoList)).thenReturn(4);
        when(todoItemRepository.findByTodoListAndOrderPositionBetween(todoList, 2, 3))
                .thenReturn(Arrays.asList(item2, item3));
        when(todoItemRepository.save(any())).thenReturn(item1);
        when(todoItemMapper.toDto(any())).thenReturn(new TodoItemDto());

        TodoItemDto result = todoListService.reorderTodoItem(1L, 1L, 1L, 3);

        assertNotNull(result);
        assertEquals(3, item1.getOrderPosition());
        assertEquals(1, item2.getOrderPosition());
        assertEquals(2, item3.getOrderPosition());
        verify(todoItemRepository).saveAll(any());
    }

    @Test
    void reorderTodoItem_SamePosition_NoChange() {
        when(userService.getUserEntityById(anyLong())).thenReturn(user);
        when(todoListRepository.findById(1L)).thenReturn(Optional.of(todoList));
        when(todoItemRepository.findByTodoListAndItemId(todoList, 2L)).thenReturn(Optional.of(item2));
        when(todoItemRepository.findMaxOrderPositionByTodoList(todoList)).thenReturn(4);
        when(todoItemMapper.toDto(any())).thenReturn(new TodoItemDto());

        TodoItemDto result = todoListService.reorderTodoItem(1L, 1L, 2L, 2);

        assertNotNull(result);
        assertEquals(2, item2.getOrderPosition());
        verify(todoItemRepository, never()).saveAll(any());
    }

    @Test
    void reorderTodoItem_InvalidPosition_ThrowsException() {
        when(userService.getUserEntityById(anyLong())).thenReturn(user);
        when(todoListRepository.findById(1L)).thenReturn(Optional.of(todoList));
        when(todoItemRepository.findByTodoListAndItemId(todoList, 2L)).thenReturn(Optional.of(item2));
        when(todoItemRepository.findMaxOrderPositionByTodoList(todoList)).thenReturn(4);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> todoListService.reorderTodoItem(1L, 1L, 2L, 5));

        assertTrue(exception.getMessage().contains("Недопустимая позиция"));
    }

    @Test
    void reorderTodoItem_TripTodoList_ChecksAccess() {
        todoList.setTrip(trip);

        when(userService.getUserEntityById(anyLong())).thenReturn(user);
        when(todoListRepository.findById(1L)).thenReturn(Optional.of(todoList));
        when(tripService.hasAccessToTrip(user, trip, "admin", "write")).thenReturn(true);
        when(todoItemRepository.findByTodoListAndItemId(todoList, 2L)).thenReturn(Optional.of(item2));
        when(todoItemRepository.findMaxOrderPositionByTodoList(todoList)).thenReturn(4);
        when(todoItemMapper.toDto(any())).thenReturn(new TodoItemDto());

        TodoItemDto result = todoListService.reorderTodoItem(1L, 1L, 2L, 2);

        assertNotNull(result);
        verify(tripService).hasAccessToTrip(user, trip, "admin", "write");
    }

    @Test
    void createTodoItem_WithPosition_InsertsCorrectly() {
        CreateTodoItemDto createDto = CreateTodoItemDto.builder()
                .content("New Item")
                .orderPosition(2)
                .build();

        when(userService.getUserEntityById(anyLong())).thenReturn(user);
        when(todoListRepository.findById(1L)).thenReturn(Optional.of(todoList));
        when(todoItemRepository.findMaxOrderPositionByTodoList(todoList)).thenReturn(3);
        when(todoItemMapper.fromCreateDto(any(), any())).thenReturn(item1);
        when(todoItemRepository.save(any())).thenReturn(item1);
        when(todoItemMapper.toDto(any())).thenReturn(new TodoItemDto());

        TodoItemDto result = todoListService.addTodoItem(1L, 1L, createDto);

        assertNotNull(result);
        verify(todoItemRepository).incrementOrderPositionsFrom(todoList, 2);
        verify(todoItemRepository).save(any());
    }

    @Test
    void createTodoItem_WithoutPosition_AddsToEnd() {
        CreateTodoItemDto createDto = CreateTodoItemDto.builder()
                .content("New Item")
                .build();

        when(userService.getUserEntityById(anyLong())).thenReturn(user);
        when(todoListRepository.findById(1L)).thenReturn(Optional.of(todoList));
        when(todoItemRepository.findMaxOrderPositionByTodoList(todoList)).thenReturn(3);
        when(todoItemMapper.fromCreateDto(any(), any())).thenReturn(item1);
        when(todoItemRepository.save(any())).thenReturn(item1);
        when(todoItemMapper.toDto(any())).thenReturn(new TodoItemDto());

        TodoItemDto result = todoListService.addTodoItem(1L, 1L, createDto);

        assertNotNull(result);
        assertEquals(4, createDto.getOrderPosition());
        verify(todoItemRepository, never()).incrementOrderPositionsFrom(any(), any());
    }

    @Test
    void deleteTodoItem_ReordersRemainingItems() {
        when(userService.getUserEntityById(anyLong())).thenReturn(user);
        when(todoListRepository.findById(1L)).thenReturn(Optional.of(todoList));
        when(todoItemRepository.findByTodoListAndItemId(todoList, 2L)).thenReturn(Optional.of(item2));

        todoListService.deleteTodoItem(1L, 1L, 2L);

        verify(todoItemRepository).delete(item2);
        verify(todoItemRepository).decrementOrderPositionsAfter(todoList, 2);
    }

    @Test
    void deleteTodoItem_LastItem_NoReordering() {
        when(userService.getUserEntityById(anyLong())).thenReturn(user);
        when(todoListRepository.findById(1L)).thenReturn(Optional.of(todoList));
        when(todoItemRepository.findByTodoListAndItemId(todoList, 4L)).thenReturn(Optional.of(item4));

        todoListService.deleteTodoItem(1L, 1L, 4L);

        verify(todoItemRepository).delete(item4);
        verify(todoItemRepository).decrementOrderPositionsAfter(todoList, 4);
    }
}