package ru.putevod.app.planner.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.putevod.app.planner.model.TodoList;
import ru.putevod.app.planner.model.Trip;
import ru.putevod.app.planner.repository.TodoListRepository;
import ru.putevod.app.planner.repository.TripRepository;
import ru.putevod.app.planner.service.DataMigrationService;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataMigrationServiceImpl implements DataMigrationService {

    private final TripRepository tripRepository;
    private final TodoListRepository todoListRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Trip> getAnonymousUserTrips(Long anonymousUserId) {
        log.info("Получение путешествий анонимного пользователя {}", anonymousUserId);
        List<Trip> trips = tripRepository.findByAnonymousCreatorId(anonymousUserId);
        log.info("Найдено {} путешествий для анонимного пользователя {}", trips.size(), anonymousUserId);
        return trips;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TodoList> getAnonymousUserTodoLists(Long anonymousUserId) {
        log.info("Получение TODO листов анонимного пользователя {}", anonymousUserId);
        List<TodoList> todoLists = todoListRepository.findByAnonymousUserId(anonymousUserId);
        log.info("Найдено {} TODO листов для анонимного пользователя {}", todoLists.size(), anonymousUserId);
        return todoLists;
    }

    @Override
    @Transactional
    public int transferTripsOwnership(Long anonymousUserId, Long registeredUserId) {
        log.info("Перенос путешествий от анонимного пользователя {} к зарегистрированному пользователю {}",
                anonymousUserId, registeredUserId);

        int updatedTrips = tripRepository.transferTripOwnership(anonymousUserId, registeredUserId);

        log.info("Перенесено {} путешествий от анонимного пользователя {} к пользователю {}",
                updatedTrips, anonymousUserId, registeredUserId);

        return updatedTrips;
    }

    @Override
    @Transactional
    public int transferTodoListsOwnership(Long anonymousUserId, Long registeredUserId) {
        log.info("Перенос TODO листов от анонимного пользователя {} к зарегистрированному пользователю {}",
                anonymousUserId, registeredUserId);

        int updatedTodoLists = todoListRepository.transferTodoListOwnership(anonymousUserId, registeredUserId);

        log.info("Перенесено {} TODO листов от анонимного пользователя {} к пользователю {}",
                updatedTodoLists, anonymousUserId, registeredUserId);

        return updatedTodoLists;
    }
} 