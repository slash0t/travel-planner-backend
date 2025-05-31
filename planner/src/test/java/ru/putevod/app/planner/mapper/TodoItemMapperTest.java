package ru.putevod.app.planner.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.putevod.app.planner.dto.TodoItemDto;
import ru.putevod.app.planner.model.TodoItem;
import ru.putevod.app.planner.model.TodoList;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class TodoItemMapperTest {

    @Autowired
    private TodoItemMapper todoItemMapper;

    @Test
    void testToDto() {
        TodoList todoList = new TodoList();
        todoList.setListId(1L);
        todoList.setTitle("Test List");
        
        TodoItem todoItem = new TodoItem();
        todoItem.setItemId(1L);
        todoItem.setContent("Test Content");
        todoItem.setCompleted(false);
        todoItem.setOrderPosition(1);
        todoItem.setTodoList(todoList);
        todoItem.setCreatedAt(LocalDateTime.now());
        todoItem.setUpdatedAt(LocalDateTime.now());

        TodoItemDto todoItemDto = todoItemMapper.toDto(todoItem);

        assertNotNull(todoItemDto);
        assertEquals(todoItem.getItemId(), todoItemDto.getId());
        assertEquals(todoItem.getTodoList().getListId(), todoItemDto.getListId());
        assertEquals(todoItem.getContent(), todoItemDto.getContent());
        assertEquals(todoItem.isCompleted(), todoItemDto.isCompleted());
        assertEquals(todoItem.getOrderPosition(), todoItemDto.getOrderPosition());
        assertEquals(todoItem.getCreatedAt(), todoItemDto.getCreatedAt());
        assertEquals(todoItem.getUpdatedAt(), todoItemDto.getUpdatedAt());
    }

    @Test
    void testToDto_NullInput() {
        TodoItemDto todoItemDto = todoItemMapper.toDto(null);

        assertNull(todoItemDto);
    }

    @Test
    void testToEntity() {
        TodoItemDto todoItemDto = TodoItemDto.builder()
                .id(1L)
                .listId(1L)
                .content("Test Content")
                .completed(false)
                .orderPosition(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        TodoItem todoItem = todoItemMapper.toEntity(todoItemDto);

        assertNotNull(todoItem);
        assertEquals(todoItemDto.getId(), todoItem.getItemId());
        assertEquals(todoItemDto.getContent(), todoItem.getContent());
        assertEquals(todoItemDto.isCompleted(), todoItem.isCompleted());
        assertEquals(todoItemDto.getOrderPosition(), todoItem.getOrderPosition());
    }

    @Test
    void testToEntity_NullInput() {
        TodoItem todoItem = todoItemMapper.toEntity(null);

        assertNull(todoItem);
    }

    @Test
    void testUpdateEntityFromDto() {
        TodoItem todoItem = new TodoItem();
        todoItem.setItemId(1L);
        todoItem.setContent("Original Content");
        todoItem.setCompleted(false);
        todoItem.setOrderPosition(1);

        TodoItemDto todoItemDto = TodoItemDto.builder()
                .content("Updated Content")
                .completed(true)
                .orderPosition(2)
                .build();

        todoItemMapper.updateEntityFromDto(todoItemDto, todoItem);

        assertEquals(1L, todoItem.getItemId());
        assertEquals(todoItemDto.getContent(), todoItem.getContent());
        assertEquals(todoItemDto.isCompleted(), todoItem.isCompleted());
        assertEquals(todoItemDto.getOrderPosition(), todoItem.getOrderPosition());
    }

    @Test
    void testUpdateEntityFromDto_NullInput() {
        TodoItem todoItem = new TodoItem();
        todoItem.setItemId(1L);
        todoItem.setContent("Original Content");
        todoItem.setCompleted(false);
        todoItem.setOrderPosition(1);

        todoItemMapper.updateEntityFromDto(null, todoItem);

        assertEquals(1L, todoItem.getItemId());
        assertEquals("Original Content", todoItem.getContent());
        assertFalse(todoItem.isCompleted());
        assertEquals(1, todoItem.getOrderPosition());
    }

    @Test
    void testFromDto() {
        TodoList todoList = new TodoList();
        todoList.setListId(1L);
        todoList.setTitle("Test List");

        TodoItemDto todoItemDto = TodoItemDto.builder()
                .id(1L)
                .listId(1L)
                .content("Test Content")
                .completed(false)
                .orderPosition(1)
                .build();

        TodoItem todoItem = todoItemMapper.fromDto(todoItemDto, todoList);

        assertNotNull(todoItem);
        assertEquals(todoItemDto.getId(), todoItem.getItemId());
        assertEquals(todoItemDto.getContent(), todoItem.getContent());
        assertEquals(todoItemDto.isCompleted(), todoItem.isCompleted());
        assertEquals(todoItemDto.getOrderPosition(), todoItem.getOrderPosition());
        assertEquals(todoList, todoItem.getTodoList());
    }

    @Test
    void testFromDto_NullInput() {
        TodoList todoList = new TodoList();
        todoList.setListId(1L);
        todoList.setTitle("Test List");

        TodoItem todoItem = todoItemMapper.fromDto(null, todoList);

        assertNull(todoItem);
    }
}
