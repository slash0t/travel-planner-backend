package ru.putevod.app.planner.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.putevod.app.planner.dto.TodoItemDto;
import ru.putevod.app.planner.dto.TodoListDto;
import ru.putevod.app.planner.model.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class TodoListMapperTest {

    @Autowired
    private TodoListMapper todoListMapper;

    @Test
    void testToDto() {
        TodoList todoList = new TodoList();
        todoList.setListId(1L);
        todoList.setTitle("Test List");
        todoList.setDescription("Test Description");
        todoList.setListType("GENERAL");
        todoList.setCreatedAt(LocalDateTime.now());
        todoList.setUpdatedAt(LocalDateTime.now());

        User user = new User();
        user.setUserId(1L);
        todoList.setUser(user);

        Trip trip = new Trip();
        trip.setTripId(1L);
        todoList.setTrip(trip);

        TodoItem todoItem = new TodoItem();
        todoItem.setItemId(1L);
        todoItem.setContent("Test Item");
        todoItem.setCompleted(false);
        todoItem.setOrderPosition(1);
        todoItem.setTodoList(todoList);
        todoItem.setCreatedAt(LocalDateTime.now());
        todoItem.setUpdatedAt(LocalDateTime.now());

        todoList.setItems(Collections.singletonList(todoItem));

        TodoListDto todoListDto = todoListMapper.toDto(todoList);

        assertNotNull(todoListDto);
        assertEquals(todoList.getListId(), todoListDto.getId());
        assertEquals(todoList.getTitle(), todoListDto.getTitle());
        assertEquals(todoList.getDescription(), todoListDto.getDescription());
        assertEquals(todoList.getListType(), todoListDto.getListType());
        assertEquals(todoList.getUser().getUserId(), todoListDto.getUserId());
        assertEquals(todoList.getTrip().getTripId(), todoListDto.getTripId());
        assertEquals(todoList.getCreatedAt(), todoListDto.getCreatedAt());
        assertEquals(todoList.getUpdatedAt(), todoListDto.getUpdatedAt());
        assertEquals(0, todoListDto.getCompletedCount());
        
        assertNotNull(todoListDto.getItems());
        assertEquals(1, todoListDto.getItems().size());
        
        TodoItemDto itemDto = todoListDto.getItems().get(0);
        assertEquals(todoItem.getItemId(), itemDto.getId());
        assertEquals(todoItem.getContent(), itemDto.getContent());
        assertEquals(todoItem.isCompleted(), itemDto.isCompleted());
        assertEquals(todoItem.getOrderPosition(), itemDto.getOrderPosition());
        assertEquals(todoItem.getTodoList().getListId(), itemDto.getListId());
    }

    @Test
    void testToDto_NullInput() {
        TodoListDto todoListDto = todoListMapper.toDto(null);

        assertNull(todoListDto);
    }

    @Test
    void testToEntity() {
        TodoListDto todoListDto = TodoListDto.builder()
                .id(1L)
                .title("Test List")
                .description("Test Description")
                .listType("GENERAL")
                .userId(1L)
                .tripId(1L)
                .items(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        TodoList todoList = todoListMapper.toEntity(todoListDto);

        assertNotNull(todoList);
        assertEquals(todoListDto.getId(), todoList.getListId());
        assertEquals(todoListDto.getTitle(), todoList.getTitle());
        assertEquals(todoListDto.getDescription(), todoList.getDescription());
        assertEquals(todoListDto.getListType(), todoList.getListType());
    }

    @Test
    void testToEntity_NullInput() {
        TodoList todoList = todoListMapper.toEntity(null);

        assertNull(todoList);
    }

    @Test
    void testUpdateEntityFromDto() {
        TodoList todoList = new TodoList();
        todoList.setListId(1L);
        todoList.setTitle("Original Title");
        todoList.setDescription("Original Description");
        todoList.setListType("GENERAL");

        TodoListDto todoListDto = TodoListDto.builder()
                .title("Updated Title")
                .description("Updated Description")
                .listType("TRIP")
                .build();

        todoListMapper.updateEntityFromDto(todoListDto, todoList);

        assertEquals(todoListDto.getTitle(), todoList.getTitle());
        assertEquals(todoListDto.getDescription(), todoList.getDescription());
        assertEquals(todoListDto.getListType(), todoList.getListType());
    }

    @Test
    void testUpdateEntityFromDto_NullInput() {
        TodoList todoList = new TodoList();
        todoList.setListId(1L);
        todoList.setTitle("Original Title");
        todoList.setDescription("Original Description");
        todoList.setListType("GENERAL");

        todoListMapper.updateEntityFromDto(null, todoList);

        assertEquals("Original Title", todoList.getTitle());
        assertEquals("Original Description", todoList.getDescription());
        assertEquals("GENERAL", todoList.getListType());
    }

    @Test
    void testFromDto() {
        TodoListDto todoListDto = TodoListDto.builder()
                .id(1L)
                .title("Test List")
                .description("Test Description")
                .listType("GENERAL")
                .build();

        User user = new User();
        user.setUserId(1L);

        Trip trip = new Trip();
        trip.setTripId(1L);

        TodoList todoList = todoListMapper.fromDto(todoListDto, user, trip);

        assertNotNull(todoList);
        assertEquals(todoListDto.getId(), todoList.getListId());
        assertEquals(todoListDto.getTitle(), todoList.getTitle());
        assertEquals(todoListDto.getDescription(), todoList.getDescription());
        assertEquals(todoListDto.getListType(), todoList.getListType());
        assertEquals(user, todoList.getUser());
        assertEquals(trip, todoList.getTrip());
    }

    @Test
    void testFromDto_NullTripInput() {
        TodoListDto todoListDto = TodoListDto.builder()
                .id(1L)
                .title("Test List")
                .description("Test Description")
                .listType("GENERAL")
                .build();

        User user = new User();
        user.setUserId(1L);

        TodoList todoList = todoListMapper.fromDto(todoListDto, user, null);

        assertNotNull(todoList);
        assertEquals(todoListDto.getId(), todoList.getListId());
        assertEquals(todoListDto.getTitle(), todoList.getTitle());
        assertEquals(todoListDto.getDescription(), todoList.getDescription());
        assertEquals(todoListDto.getListType(), todoList.getListType());
        assertEquals(user, todoList.getUser());
        assertNull(todoList.getTrip());
    }

    @Test
    void testFromDto_NullInput() {
        User user = new User();
        user.setUserId(1L);

        Trip trip = new Trip();
        trip.setTripId(1L);

        TodoList todoList = todoListMapper.fromDto(null, user, trip);

        assertNull(todoList);
    }
}
