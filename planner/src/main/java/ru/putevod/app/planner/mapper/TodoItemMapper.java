package ru.putevod.app.planner.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.putevod.app.planner.dto.TodoItemDto;
import ru.putevod.app.planner.model.TodoItem;
import ru.putevod.app.planner.model.TodoList;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TodoItemMapper {

    @Mapping(source = "itemId", target = "id")
    @Mapping(source = "todoList.listId", target = "listId")
    TodoItemDto toDto(TodoItem todoItem);

    @Mapping(source = "id", target = "itemId")
    @Mapping(source = "content", target = "content")
    @Mapping(source = "completed", target = "completed")
    @Mapping(source = "orderPosition", target = "orderPosition")
    @Mapping(target = "todoList", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    TodoItem toEntity(TodoItemDto todoItemDto);

    @Mapping(target = "itemId", ignore = true)
    @Mapping(source = "content", target = "content")
    @Mapping(source = "completed", target = "completed")
    @Mapping(source = "orderPosition", target = "orderPosition")
    @Mapping(target = "todoList", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(TodoItemDto todoItemDto, @MappingTarget TodoItem todoItem);

    default TodoItem fromDto(TodoItemDto todoItemDto, TodoList todoList) {
        if (todoItemDto == null) {
            return null;
        }

        TodoItem todoItem = toEntity(todoItemDto);
        todoItem.setTodoList(todoList);

        return todoItem;
    }
} 