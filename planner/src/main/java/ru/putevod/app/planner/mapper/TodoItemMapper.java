package ru.putevod.app.planner.mapper;

import org.mapstruct.*;
import ru.putevod.app.planner.dto.CreateTodoItemDto;
import ru.putevod.app.planner.dto.TodoItemDto;
import ru.putevod.app.planner.dto.UpdateTodoItemDto;
import ru.putevod.app.planner.model.TodoItem;
import ru.putevod.app.planner.model.TodoList;

@Mapper(
        config = MapstructConfig.class,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface TodoItemMapper {

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(source = "itemId", target = "id")
    @Mapping(source = "todoList.listId", target = "listId")
    TodoItemDto toDto(TodoItem todoItem);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(source = "id", target = "itemId")
    @Mapping(source = "content", target = "content")
    @Mapping(source = "completed", target = "completed")
    @Mapping(source = "orderPosition", target = "orderPosition")
    @Mapping(target = "todoList", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    TodoItem toEntity(TodoItemDto todoItemDto);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(target = "itemId", ignore = true)
    @Mapping(source = "content", target = "content")
    @Mapping(source = "completed", target = "completed")
    @Mapping(source = "orderPosition", target = "orderPosition")
    @Mapping(target = "todoList", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    TodoItem toEntityFromCreate(CreateTodoItemDto createTodoItemDto);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(target = "itemId", ignore = true)
    @Mapping(source = "content", target = "content")
    @Mapping(source = "completed", target = "completed")
    @Mapping(source = "orderPosition", target = "orderPosition")
    @Mapping(target = "todoList", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(TodoItemDto todoItemDto, @MappingTarget TodoItem todoItem);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(target = "itemId", ignore = true)
    @Mapping(source = "content", target = "content")
    @Mapping(source = "completed", target = "completed")
    @Mapping(source = "orderPosition", target = "orderPosition")
    @Mapping(target = "todoList", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromUpdate(UpdateTodoItemDto updateTodoItemDto, @MappingTarget TodoItem todoItem);

    default TodoItem fromDto(TodoItemDto todoItemDto, TodoList todoList) {
        if (todoItemDto == null) {
            return null;
        }

        TodoItem todoItem = toEntity(todoItemDto);
        todoItem.setTodoList(todoList);

        return todoItem;
    }

    default TodoItem fromCreateDto(CreateTodoItemDto createTodoItemDto, TodoList todoList) {
        if (createTodoItemDto == null) {
            return null;
        }

        TodoItem todoItem = toEntityFromCreate(createTodoItemDto);
        todoItem.setTodoList(todoList);

        return todoItem;
    }
} 