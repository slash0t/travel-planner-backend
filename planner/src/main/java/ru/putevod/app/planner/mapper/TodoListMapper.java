package ru.putevod.app.planner.mapper;

import org.mapstruct.*;
import ru.putevod.app.planner.dto.CreateTodoListDto;
import ru.putevod.app.planner.dto.TodoListDto;
import ru.putevod.app.planner.model.TodoList;
import ru.putevod.app.planner.model.Trip;
import ru.putevod.app.planner.model.User;

@Mapper(
        config = MapstructConfig.class,
        uses = {TodoItemMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface TodoListMapper {

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(source = "listId", target = "id")
    @Mapping(source = "user.userId", target = "userId")
    @Mapping(source = "trip.tripId", target = "tripId")
    @Mapping(target = "itemCount", ignore = true)
    @Mapping(target = "completedCount", ignore = true)
    @Named("toDto")
    TodoListDto toDto(TodoList todoList);

    @AfterMapping
    default void addCounts(@MappingTarget TodoListDto dto, TodoList entity) {
        dto.setItemCount(entity.getItemCount());
        dto.setCompletedCount(entity.getCompletedCount());
    }

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
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

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
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

    // Маппинг из CreateTodoListDto в TodoList
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(target = "listId", ignore = true)
    @Mapping(source = "createTodoListDto.title", target = "title")
    @Mapping(source = "createTodoListDto.description", target = "description")
    @Mapping(source = "createTodoListDto.listType", target = "listType")
    @Mapping(source = "user", target = "user")
    @Mapping(source = "trip", target = "trip")
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    TodoList toEntityFromCreate(CreateTodoListDto createTodoListDto, User user, Trip trip);

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

    default TodoList fromCreateDto(CreateTodoListDto createTodoListDto, User user, Trip trip) {
        if (createTodoListDto == null) {
            return null;
        }

        return toEntityFromCreate(createTodoListDto, user, trip);
    }

    // Специальный метод для обработки TodoList с проверкой удаленных поездок
    @Named("toDtoWithTripCheck")
    default TodoListDto toDtoWithTripCheck(TodoList todoList) {
        if (todoList == null) {
            return null;
        }

        TodoListDto dto = toDto(todoList);

        // Если поездка удалена, убираем tripId
        if (todoList.getTrip() != null && todoList.getTrip().isDeleted()) {
            Long originalTripId = dto.getTripId();
            dto.setTripId(null);
            // Логируем для отладки (если нужно, можно убрать позже)
            System.out.println("Удален tripId " + originalTripId + " для TodoList " + dto.getId() +
                    " так как поездка помечена как удаленная");
        }

        return dto;
    }
} 