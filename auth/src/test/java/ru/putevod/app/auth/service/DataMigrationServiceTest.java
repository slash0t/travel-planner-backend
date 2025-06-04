package ru.putevod.app.auth.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.putevod.app.auth.client.PlannerClient;
import ru.putevod.app.auth.service.impl.DataMigrationServiceImpl;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataMigrationServiceTest {

    @Mock
    private PlannerClient plannerClient;

    @InjectMocks
    private DataMigrationServiceImpl dataMigrationService;

    @Test
    void migrateAnonymousUserData_shouldCompleteSuccessfully() {
        Long anonymousUserId = 123L;
        Integer registeredUserId = 456;

        // Настраиваем мок только для этого теста
        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("transferredTrips", 2);
        successResponse.put("transferredTodoLists", 1);
        successResponse.put("message", "Данные успешно мигрированы");
        
        when(plannerClient.migrateAllData(anyLong(), anyLong())).thenReturn(successResponse);

        assertDoesNotThrow(() -> {
            dataMigrationService.migrateAnonymousUserData(anonymousUserId, registeredUserId);
        });
    }

    @Test
    void migrateTrips_shouldCompleteSuccessfully() {
        Long anonymousUserId = 123L;
        Integer registeredUserId = 456;

        // Настраиваем мок только для этого теста
        Map<String, Object> tripsResponse = new HashMap<>();
        tripsResponse.put("transferredTrips", 2);
        tripsResponse.put("message", "Путешествия успешно перенесены");
        
        when(plannerClient.transferTripsOwnership(anyLong(), anyLong())).thenReturn(tripsResponse);

        assertDoesNotThrow(() -> {
            dataMigrationService.migrateTrips(anonymousUserId, registeredUserId);
        });
    }

    @Test
    void migrateTodoLists_shouldCompleteSuccessfully() {
        Long anonymousUserId = 123L;
        Integer registeredUserId = 456;

        // Настраиваем мок только для этого теста
        Map<String, Object> todoResponse = new HashMap<>();
        todoResponse.put("transferredTodoLists", 1);
        todoResponse.put("message", "TODO листы успешно перенесены");
        
        when(plannerClient.transferTodoListsOwnership(anyLong(), anyLong())).thenReturn(todoResponse);

        assertDoesNotThrow(() -> {
            dataMigrationService.migrateTodoLists(anonymousUserId, registeredUserId);
        });
    }
} 