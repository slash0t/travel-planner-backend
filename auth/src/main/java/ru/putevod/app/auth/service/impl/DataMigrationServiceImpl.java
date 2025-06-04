package ru.putevod.app.auth.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.putevod.app.auth.client.PlannerClient;
import ru.putevod.app.auth.service.DataMigrationService;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataMigrationServiceImpl implements DataMigrationService {

    private final PlannerClient plannerClient;

    @Override
    @Transactional
    public void migrateAnonymousUserData(Long anonymousUserId, Integer registeredUserId) {
        log.info("Начинаем миграцию данных анонимного пользователя {} к зарегистрированному пользователю {}",
                anonymousUserId, registeredUserId);

        try {
            // Вызываем полную миграцию через planner сервис
            Map<String, Object> result = plannerClient.migrateAllData(anonymousUserId, registeredUserId.longValue());

            Integer transferredTrips = (Integer) result.get("transferredTrips");
            Integer transferredTodoLists = (Integer) result.get("transferredTodoLists");

            log.info("Миграция данных анонимного пользователя {} к зарегистрированному пользователю {} завершена успешно. " +
                            "Перенесено путешествий: {}, TODO листов: {}",
                    anonymousUserId, registeredUserId, transferredTrips, transferredTodoLists);

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
            Map<String, Object> result = plannerClient.transferTripsOwnership(anonymousUserId, registeredUserId.longValue());

            Integer transferredTrips = (Integer) result.get("transferredTrips");

            log.info("Миграция путешествий анонимного пользователя {} завершена. Перенесено путешествий: {}",
                    anonymousUserId, transferredTrips);

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
            Map<String, Object> result = plannerClient.transferTodoListsOwnership(anonymousUserId, registeredUserId.longValue());

            Integer transferredTodoLists = (Integer) result.get("transferredTodoLists");

            log.info("Миграция TODO листов анонимного пользователя {} завершена. Перенесено TODO листов: {}",
                    anonymousUserId, transferredTodoLists);

        } catch (Exception e) {
            log.error("Ошибка при миграции TODO листов анонимного пользователя {}: {}", anonymousUserId, e.getMessage(), e);
            throw e;
        }
    }
} 