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

        return todoListMapper.toDtoSafe(todoList);
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

        return todoListMapper.toDtoSafe(todoList);
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

        return todoListMapper.toDtoSafe(todoList);
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

        return todoListMapper.toDtoSafe(todoList);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TodoListDto> getUserTodoLists(Long userId, Pageable pageable) {
        log.info("Начало получения списков задач для пользователя ID: {}", userId);
        
        try {
            User user = userService.getUserEntityById(userId);
            log.info("Пользователь найден: username={}, email={}", user.getUsername(), user.getEmail());

            Page<TodoList> todoLists;
            
            try {
                todoLists = todoListRepository.findAllActiveByUser(user, pageable);
                log.info("Найдено {} списков задач для пользователя {} (основной запрос)", todoLists.getTotalElements(), userId);
            } catch (Exception e) {
                log.warn("Ошибка при получении списков, переключаемся на fallback: {}", e.getMessage());
                
                try {
                    todoLists = todoListRepository.findAllByUserSimple(user, pageable);
                    log.info("Найдено {} списков задач для пользователя {} (кастомный запрос)", todoLists.getTotalElements(), userId);
                } catch (Exception e2) {
                    log.warn("Ошибка с кастомным запросом, используем стандартный метод Spring Data: {}", e2.getMessage());
                    todoLists = todoListRepository.findByUserOrderByCreatedAtDesc(user, pageable);
                    log.info("Найдено {} списков задач для пользователя {} (Spring Data метод)", todoLists.getTotalElements(), userId);
                }
            }

            // Используем специальный маппер с проверкой удаленных поездок
            Page<TodoListDto> result = todoLists.map(todoListMapper::toDtoSafe);
            log.info("Успешно преобразованы списки задач в DTO для пользователя {} (с обработкой удаленных поездок)", userId);
            return result;
            
        } catch (Exception e) {
            log.error("Ошибка при получении списков задач для пользователя {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TodoListDto> getUserTodoListsSimple(Long userId, Pageable pageable) {
        log.info("DEBUG: Простое получение списков задач для пользователя ID: {}", userId);
        
        try {
            User user = userService.getUserEntityById(userId);
            log.info("DEBUG: Пользователь найден: username={}, email={}", user.getUsername(), user.getEmail());

            Page<TodoList> todoLists = todoListRepository.findAllByUserSimple(user, pageable);
            log.info("DEBUG: Найдено {} простых списков задач для пользователя {}", todoLists.getTotalElements(), userId);

            // Используем специальный маппер с проверкой удаленных поездок
            Page<TodoListDto> result = todoLists.map(todoListMapper::toDtoSafe);
            log.info("DEBUG: Успешно преобразованы простые списки задач в DTO для пользователя {} (с обработкой удаленных поездок)", userId);
            return result;
            
        } catch (Exception e) {
            log.error("DEBUG: Ошибка при получении простых списков задач для пользователя {}: {}", userId, e.getMessage(), e);
            throw e;
        }
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
                .map(todoListMapper::toDtoSafe)
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

        if (todoList.getTrip() != null) {
            if (!tripService.hasAccessToTrip(user, todoList.getTrip(), "admin", "write")) {
                throw new BadRequestException("У вас нет прав на добавление задач в этот список");
            }
        } else if (!todoList.getUser().getUserId().equals(userId)) {
            throw new BadRequestException("У вас нет прав на добавление задач в этот список");
        }

        if (createTodoItemDto.getOrderPosition() == null) {
            Integer maxPosition = todoItemRepository.findMaxOrderPositionByTodoList(todoList);
            createTodoItemDto.setOrderPosition(maxPosition + 1);
        } else {
            Integer newPosition = createTodoItemDto.getOrderPosition();
            Integer maxPosition = todoItemRepository.findMaxOrderPositionByTodoList(todoList);
            
            if (newPosition <= maxPosition) {
                todoItemRepository.incrementOrderPositionsFrom(todoList, newPosition);
            }
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
        todoItemRepository.updateCompletionStatus(todoList, itemId, newStatus);

       todoItem.setCompleted(newStatus);

        return todoItemMapper.toDto(todoItem);
    }

    @Override
    @Transactional
    public void toggleAllTodoItemsComplete(Long userId, Long listId, boolean completed) {
        User user = userService.getUserEntityById(userId);

        TodoList todoList = todoListRepository.findById(listId)
                .orElseThrow(() -> new ResourceNotFoundException("Список задач", "id", listId));

        if (todoList.getTrip() != null) {
            if (!tripService.hasAccessToTrip(user, todoList.getTrip(), "admin", "write")) {
                throw new BadRequestException("У вас нет прав на изменение статуса задач в этом списке");
            }
        } else if (!todoList.getUser().getUserId().equals(userId)) {
            throw new BadRequestException("У вас нет прав на изменение статуса задач в этом списке");
        }

        todoItemRepository.updateAllCompletionStatus(todoList, completed);

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

        if (todoList.getTrip() != null) {
            if (!tripService.hasAccessToTrip(user, todoList.getTrip(), "admin", "write")) {
                throw new BadRequestException("У вас нет прав на удаление задач из этого списка");
            }
        } else if (!todoList.getUser().getUserId().equals(userId)) {
            throw new BadRequestException("У вас нет прав на удаление задач из этого списка");
        }

        TodoItem todoItem = todoItemRepository.findByTodoListAndItemId(todoList, itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Задача", "id", itemId));

        Integer deletedPosition = todoItem.getOrderPosition();
        todoItemRepository.delete(todoItem);
        
        todoItemRepository.decrementOrderPositionsAfter(todoList, deletedPosition);
    }

    @Override
    @Transactional
    public TodoItemDto reorderTodoItem(Long userId, Long listId, Long itemId, Integer newPosition) {
        User user = userService.getUserEntityById(userId);

        TodoList todoList = todoListRepository.findById(listId)
                .orElseThrow(() -> new ResourceNotFoundException("Список задач", "id", listId));

        if (todoList.getTrip() != null) {
            if (!tripService.hasAccessToTrip(user, todoList.getTrip(), "admin", "write")) {
                throw new BadRequestException("У вас нет прав на изменение порядка задач в этом списке");
            }
        } else if (!todoList.getUser().getUserId().equals(userId)) {
            throw new BadRequestException("У вас нет прав на изменение порядка задач в этом списке");
        }

        TodoItem todoItem = todoItemRepository.findByTodoListAndItemId(todoList, itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Задача", "id", itemId));

        Integer currentPosition = todoItem.getOrderPosition();
        Integer maxPosition = todoItemRepository.findMaxOrderPositionByTodoList(todoList);

        if (newPosition < 1 || newPosition > maxPosition) {
            throw new BadRequestException("Недопустимая позиция. Позиция должна быть от 1 до " + maxPosition);
        }

        if (currentPosition.equals(newPosition)) {
            return todoItemMapper.toDto(todoItem);
        }

        if (newPosition < currentPosition) {
            List<TodoItem> itemsToShift = todoItemRepository
                    .findByTodoListAndOrderPositionBetween(todoList, newPosition, currentPosition - 1);
            
            for (TodoItem item : itemsToShift) {
                item.setOrderPosition(item.getOrderPosition() + 1);
            }
            todoItemRepository.saveAll(itemsToShift);
        } else {
            List<TodoItem> itemsToShift = todoItemRepository
                    .findByTodoListAndOrderPositionBetween(todoList, currentPosition + 1, newPosition);
            
            for (TodoItem item : itemsToShift) {
                item.setOrderPosition(item.getOrderPosition() - 1);
            }
            todoItemRepository.saveAll(itemsToShift);
        }

        todoItem.setOrderPosition(newPosition);
        todoItem = todoItemRepository.save(todoItem);

        return todoItemMapper.toDto(todoItem);
    }
}