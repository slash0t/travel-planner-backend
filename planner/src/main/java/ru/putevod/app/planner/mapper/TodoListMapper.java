package ru.putevod.app.planner.mapper;

import org.mapstruct.*;
import ru.putevod.app.planner.dto.TodoListDto;
import ru.putevod.app.planner.model.TodoList;
import ru.putevod.app.planner.model.Trip;
import ru.putevod.app.planner.model.User;

@Mapper(componentModel = "spring",
        uses = {TodoItemMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TodoListMapper {

    @Mapping(source = "listId", target = "id")
    @Mapping(source = "user.userId", target = "userId")
    @Mapping(source = "trip.tripId", target = "tripId")
    @Mapping(target = "itemCount", ignore = true)
    @Mapping(target = "completedCount", ignore = true)
    TodoListDto toDto(TodoList todoList);

    @AfterMapping
    default void addCounts(@MappingTarget TodoListDto dto, TodoList entity) {
        dto.setItemCount(entity.getItemCount());
        dto.setCompletedCount(entity.getCompletedCount());
    }

    @Mapping(source = "id", target = "listId")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "listType", target = "listType")
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "trip", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    TodoList toEntity(TodoListDto todoListDto);

    @Mapping(target = "listId", ignore = true)
    @Mapping(source = "title", target = "title")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "listType", target = "listType")
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "trip", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(TodoListDto todoListDto, @MappingTarget TodoList todoList);

    default TodoList fromDto(TodoListDto todoListDto, User user, Trip trip) {
        if (todoListDto == null) {
            return null;
        }

        TodoList todoList = toEntity(todoListDto);
        todoList.setUser(user);
        if (trip != null) {
            todoList.setTrip(trip);
        }

        return todoList;
    }
} 