package ru.putevod.app.auth.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.putevod.app.auth.service.DataMigrationService;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataMigrationServiceImpl implements DataMigrationService {

    // TODO: Добавить клиенты для взаимодействия с planner сервисом когда они будут доступны
    // private final PlannerClient plannerClient;

    @Override
    @Transactional
    public void migrateAnonymousUserData(Long anonymousUserId, Integer registeredUserId) {
        log.info("Начинаем миграцию данных анонимного пользователя {} к зарегистрированному пользователю {}", 
                anonymousUserId, registeredUserId);
        
        try {
            // Мигрируем путешествия
            migrateTrips(anonymousUserId, registeredUserId);
            
            // Мигрируем TODO листы
            migrateTodoLists(anonymousUserId, registeredUserId);
            
            log.info("Миграция данных анонимного пользователя {} к зарегистрированному пользователю {} завершена успешно", 
                    anonymousUserId, registeredUserId);
            
        } catch (Exception e) {
            log.error("Ошибка при миграции данных анонимного пользователя {} к зарегистрированному пользователю {}: {}", 
                    anonymousUserId, registeredUserId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void migrateTrips(Long anonymousUserId, Integer registeredUserId) {
        log.info("Начинаем миграцию путешествий анонимного пользователя {} к зарегистрированному пользователю {}", 
                anonymousUserId, registeredUserId);
        
        try {
            // TODO: Реализовать миграцию путешествий через вызов к planner сервису
            // Пример:
            // List<Trip> anonymousTrips = plannerClient.getAnonymousUserTrips(anonymousUserId);
            // for (Trip trip : anonymousTrips) {
            //     plannerClient.transferTripOwnership(trip.getId(), anonymousUserId, registeredUserId.longValue());
            // }
            
            log.info("Миграция путешествий анонимного пользователя {} завершена. " +
                    "Заглушка - реальная миграция будет реализована после создания Feign клиентов", anonymousUserId);
            
        } catch (Exception e) {
            log.error("Ошибка при миграции путешествий анонимного пользователя {}: {}", anonymousUserId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void migrateTodoLists(Long anonymousUserId, Integer registeredUserId) {
        log.info("Начинаем миграцию TODO листов анонимного пользователя {} к зарегистрированному пользователю {}", 
                anonymousUserId, registeredUserId);
        
        try {
            // TODO: Реализовать миграцию TODO листов через вызов к planner сервису
            // Пример:
            // List<TodoList> anonymousTodoLists = plannerClient.getAnonymousUserTodoLists(anonymousUserId);
            // for (TodoList todoList : anonymousTodoLists) {
            //     plannerClient.transferTodoListOwnership(todoList.getId(), anonymousUserId, registeredUserId.longValue());
            // }
            
            log.info("Миграция TODO листов анонимного пользователя {} завершена. " +
                    "Заглушка - реальная миграция будет реализована после создания Feign клиентов", anonymousUserId);
            
        } catch (Exception e) {
            log.error("Ошибка при миграции TODO листов анонимного пользователя {}: {}", anonymousUserId, e.getMessage(), e);
            throw e;
        }
    }
} 