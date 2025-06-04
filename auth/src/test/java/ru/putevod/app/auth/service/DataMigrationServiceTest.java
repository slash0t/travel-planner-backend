package ru.putevod.app.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.putevod.app.auth.service.impl.DataMigrationServiceImpl;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DataMigrationServiceTest {

    @InjectMocks
    private DataMigrationServiceImpl dataMigrationService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void migrateAnonymousUserData_shouldCompleteSuccessfully() {
        Long anonymousUserId = 123L;
        Integer registeredUserId = 456;

        assertDoesNotThrow(() -> {
            dataMigrationService.migrateAnonymousUserData(anonymousUserId, registeredUserId);
        });
    }

    @Test
    void migrateTrips_shouldCompleteSuccessfully() {
        Long anonymousUserId = 123L;
        Integer registeredUserId = 456;

        assertDoesNotThrow(() -> {
            dataMigrationService.migrateTrips(anonymousUserId, registeredUserId);
        });
    }

    @Test
    void migrateTodoLists_shouldCompleteSuccessfully() {
        Long anonymousUserId = 123L;
        Integer registeredUserId = 456;

        assertDoesNotThrow(() -> {
            dataMigrationService.migrateTodoLists(anonymousUserId, registeredUserId);
        });
    }
} 