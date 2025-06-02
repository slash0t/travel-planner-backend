package ru.putevod.app.planner.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.putevod.app.planner.dto.CreateTodoItemDto;
import ru.putevod.app.planner.dto.TodoItemDto;
import ru.putevod.app.planner.dto.TodoListDto;
import ru.putevod.app.planner.dto.UpdateTodoItemDto;
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
import ru.putevod.app.planner.service.TodoListService;
import ru.putevod.app.planner.service.TripService;
import ru.putevod.app.planner.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TodoListServiceImpl implements TodoListService {

    private final TodoListRepository todoListRepository;
    private final TodoItemRepository todoItemRepository;
    private final UserService userService;
    private final TripService tripService;
    private final TodoListMapper todoListMapper;
    private final TodoItemMapper todoItemMapper;

    @Override
    @Transactional
    public TodoListDto createTodoList(Long userId, TodoListDto todoListDto) {
        User user = userService.getUserEntityById(userId);

        TodoList todoList = todoListMapper.toEntity(todoListDto);
        todoList.setUser(user);

        todoList = todoListRepository.save(todoList);

        return todoListMapper.toDto(todoList);
    }

    @Override
    @Transactional
    public TodoListDto createTripTodoList(Long userId, Long tripId, TodoListDto todoListDto) {
        User user = userService.getUserEntityById(userId);
        Trip trip = tripService.getTripEntityWithAccessCheck(userId, tripId);

        // Проверяем доступ на запись
        if (!tripService.hasAccessToTrip(user, trip, "admin", "write")) {
            throw new BadRequestException("У вас нет прав на создание списков задач в этой поездке");
        }

        TodoList todoList = todoListMapper.toEntity(todoListDto);
        todoList.setUser(user);
        todoList.setTrip(trip);

        todoList = todoListRepository.save(todoList);

        return todoListMapper.toDto(todoList);
    }

    @Override
    @Transactional
    public TodoListDto updateTodoList(Long userId, Long listId, TodoListDto todoListDto) {
        User user = userService.getUserEntityById(userId);

        TodoList todoList = todoListRepository.findByUserAndListId(user, listId)
                .orElseThrow(() -> new ResourceNotFoundException("Список задач", "id", listId));

        // Если список принадлежит поездке, проверяем права доступа
        if (todoList.getTrip() != null) {
            if (!tripService.hasAccessToTrip(user, todoList.getTrip(), "admin", "write")) {
                throw new BadRequestException("У вас нет прав на редактирование этого списка задач");
            }
        }

        todoListMapper.updateEntityFromDto(todoListDto, todoList);
        todoList = todoListRepository.save(todoList);

        return todoListMapper.toDto(todoList);
    }

    @Override
    @Transactional(readOnly = true)
    public TodoListDto getTodoListById(Long userId, Long listId) {
        User user = userService.getUserEntityById(userId);

        TodoList todoList = todoListRepository.findById(listId)
                .orElseThrow(() -> new ResourceNotFoundException("Список задач", "id", listId));

        // Проверяем доступ к списку
        if (todoList.getTrip() != null) {
            if (!tripService.hasAccessToTrip(user, todoList.getTrip(), "admin", "read", "write")) {
                throw new BadRequestException("У вас нет доступа к этому списку задач");
            }
        } else if (!todoList.getUser().getUserId().equals(userId)) {
            throw new BadRequestException("У вас нет доступа к этому списку задач");
        }

        return todoListMapper.toDto(todoList);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TodoListDto> getUserTodoLists(Long userId, Pageable pageable) {
        User user = userService.getUserEntityById(userId);

        // Получаем все активные списки пользователя (в т.ч. из неудаленных поездок)
        Page<TodoList> todoLists = todoListRepository.findAllActiveByUser(user, pageable);

        return todoLists.map(todoListMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TodoListDto> getTripTodoLists(Long userId, Long tripId) {
        User user = userService.getUserEntityById(userId);
        Trip trip = tripService.getTripEntityWithAccessCheck(userId, tripId);

        // Проверяем доступ на чтение
        if (!tripService.hasAccessToTrip(user, trip, "admin", "read", "write")) {
            throw new BadRequestException("У вас нет прав на просмотр списков задач в этой поездке");
        }

        List<TodoList> todoLists = trip.getTodoLists();

        return todoLists.stream()
                .map(todoListMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteTodoList(Long userId, Long listId) {
        User user = userService.getUserEntityById(userId);

        TodoList todoList = todoListRepository.findById(listId)
                .orElseThrow(() -> new ResourceNotFoundException("Список задач", "id", listId));

        // Проверяем права на удаление
        if (todoList.getTrip() != null) {
            if (!tripService.hasAccessToTrip(user, todoList.getTrip(), "admin", "write")) {
                throw new BadRequestException("У вас нет прав на удаление этого списка задач");
            }
        } else if (!todoList.getUser().getUserId().equals(userId)) {
            throw new BadRequestException("У вас нет прав на удаление этого списка задач");
        }

        todoListRepository.delete(todoList);
    }

    @Override
    @Transactional
    public TodoItemDto addTodoItem(Long userId, Long listId, CreateTodoItemDto createTodoItemDto) {
        User user = userService.getUserEntityById(userId);

        TodoList todoList = todoListRepository.findById(listId)
                .orElseThrow(() -> new ResourceNotFoundException("Список задач", "id", listId));

        // Проверяем права на добавление элементов
        if (todoList.getTrip() != null) {
            if (!tripService.hasAccessToTrip(user, todoList.getTrip(), "admin", "write")) {
                throw new BadRequestException("У вас нет прав на добавление задач в этот список");
            }
        } else if (!todoList.getUser().getUserId().equals(userId)) {
            throw new BadRequestException("У вас нет прав на добавление задач в этот список");
        }

        // Если позиция не указана, устанавливаем в конец списка
        if (createTodoItemDto.getOrderPosition() == null) {
            Integer maxPosition = todoItemRepository.findByTodoListOrderByOrderPositionAsc(todoList).stream()
                    .map(TodoItem::getOrderPosition)
                    .max(Integer::compareTo)
                    .orElse(0);

            createTodoItemDto.setOrderPosition(maxPosition + 1);
        }

        TodoItem todoItem = todoItemMapper.fromCreateDto(createTodoItemDto, todoList);
        todoItem = todoItemRepository.save(todoItem);

        return todoItemMapper.toDto(todoItem);
    }

    @Override
    @Transactional
    public TodoItemDto updateTodoItem(Long userId, Long listId, Long itemId, UpdateTodoItemDto updateTodoItemDto) {
        User user = userService.getUserEntityById(userId);

        TodoList todoList = todoListRepository.findById(listId)
                .orElseThrow(() -> new ResourceNotFoundException("Список задач", "id", listId));

        // Проверяем права на редактирование элементов
        if (todoList.getTrip() != null) {
            if (!tripService.hasAccessToTrip(user, todoList.getTrip(), "admin", "write")) {
                throw new BadRequestException("У вас нет прав на редактирование задач в этом списке");
            }
        } else if (!todoList.getUser().getUserId().equals(userId)) {
            throw new BadRequestException("У вас нет прав на редактирование задач в этом списке");
        }

        TodoItem todoItem = todoItemRepository.findByTodoListAndItemId(todoList, itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Задача", "id", itemId));

        todoItemMapper.updateEntityFromUpdate(updateTodoItemDto, todoItem);
        todoItem = todoItemRepository.save(todoItem);

        return todoItemMapper.toDto(todoItem);
    }

    @Override
    @Transactional
    public TodoItemDto toggleTodoItemComplete(Long userId, Long listId, Long itemId) {
        User user = userService.getUserEntityById(userId);

        TodoList todoList = todoListRepository.findById(listId)
                .orElseThrow(() -> new ResourceNotFoundException("Список задач", "id", listId));

        // Проверяем права на редактирование элементов
        if (todoList.getTrip() != null) {
            if (!tripService.hasAccessToTrip(user, todoList.getTrip(), "admin", "write", "read")) {
                throw new BadRequestException("У вас нет прав на изменение статуса задач в этом списке");
            }
        } else if (!todoList.getUser().getUserId().equals(userId)) {
            throw new BadRequestException("У вас нет прав на изменение статуса задач в этом списке");
        }

        TodoItem todoItem = todoItemRepository.findByTodoListAndItemId(todoList, itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Задача", "id", itemId));

        boolean newStatus = !todoItem.isCompleted();
        todoItem.setCompleted(newStatus);
        newStatus = !todoItem.isCompleted();
        todoItemRepository.updateCompletionStatus(todoList, itemId, newStatus);

        // Обновляем объект в памяти после обновления в БД
        todoItem.setCompleted(newStatus);

        return todoItemMapper.toDto(todoItem);
    }

    @Override
    @Transactional
    public void toggleAllTodoItemsComplete(Long userId, Long listId, boolean completed) {
        User user = userService.getUserEntityById(userId);

        TodoList todoList = todoListRepository.findById(listId)
                .orElseThrow(() -> new ResourceNotFoundException("Список задач", "id", listId));

        // Проверяем права на редактирование элементов
        if (todoList.getTrip() != null) {
            if (!tripService.hasAccessToTrip(user, todoList.getTrip(), "admin", "write")) {
                throw new BadRequestException("У вас нет прав на изменение статуса задач в этом списке");
            }
        } else if (!todoList.getUser().getUserId().equals(userId)) {
            throw new BadRequestException("У вас нет прав на изменение статуса задач в этом списке");
        }

        // Изменяем статус всех элементов списка
        todoItemRepository.updateAllCompletionStatus(todoList, completed);

        // Обновляем статусы и в объектах в памяти
        for (TodoItem item : todoList.getItems()) {
            item.setCompleted(completed);
        }
    }

    @Override
    @Transactional
    public void deleteTodoItem(Long userId, Long listId, Long itemId) {
        User user = userService.getUserEntityById(userId);

        TodoList todoList = todoListRepository.findById(listId)
                .orElseThrow(() -> new ResourceNotFoundException("Список задач", "id", listId));

        // Проверяем права на удаление элементов
        if (todoList.getTrip() != null) {
            if (!tripService.hasAccessToTrip(user, todoList.getTrip(), "admin", "write")) {
                throw new BadRequestException("У вас нет прав на удаление задач из этого списка");
            }
        } else if (!todoList.getUser().getUserId().equals(userId)) {
            throw new BadRequestException("У вас нет прав на удаление задач из этого списка");
        }

        TodoItem todoItem = todoItemRepository.findByTodoListAndItemId(todoList, itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Задача", "id", itemId));

        todoItemRepository.delete(todoItem);
    }
} 