package ru.putevod.app.planner.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.putevod.app.planner.model.TodoList;
import ru.putevod.app.planner.model.Trip;
import ru.putevod.app.planner.repository.TodoListRepository;
import ru.putevod.app.planner.repository.TripRepository;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataMigrationServiceImplTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private TodoListRepository todoListRepository;

    @InjectMocks
    private DataMigrationServiceImpl dataMigrationService;

    private Long anonymousUserId;
    private Long registeredUserId;
    private Trip testTrip;
    private TodoList testTodoList;

    @BeforeEach
    void setUp() {
        anonymousUserId = 1L;
        registeredUserId = 2L;

        testTrip = new Trip();
        testTrip.setTripId(1L);
        testTrip.setAnonymousCreatorId(anonymousUserId);

        testTodoList = new TodoList();
        testTodoList.setListId(1L);
        testTodoList.setAnonymousUserId(anonymousUserId);
    }

    @Test
    @DisplayName("Should get anonymous user trips successfully")
    void getAnonymousUserTrips_Success() {
        List<Trip> expectedTrips = Arrays.asList(testTrip);
        when(tripRepository.findByAnonymousCreatorId(anonymousUserId)).thenReturn(expectedTrips);

        List<Trip> result = dataMigrationService.getAnonymousUserTrips(anonymousUserId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTrip.getTripId(), result.get(0).getTripId());
        verify(tripRepository).findByAnonymousCreatorId(anonymousUserId);
    }

    @Test
    @DisplayName("Should get anonymous user todo lists successfully")
    void getAnonymousUserTodoLists_Success() {
        List<TodoList> expectedTodoLists = Arrays.asList(testTodoList);
        when(todoListRepository.findByAnonymousUserId(anonymousUserId)).thenReturn(expectedTodoLists);

        List<TodoList> result = dataMigrationService.getAnonymousUserTodoLists(anonymousUserId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTodoList.getListId(), result.get(0).getListId());
        verify(todoListRepository).findByAnonymousUserId(anonymousUserId);
    }

    @Test
    @DisplayName("Should transfer trips ownership successfully")
    void transferTripsOwnership_Success() {
        int expectedUpdatedTrips = 2;
        when(tripRepository.transferTripOwnership(anonymousUserId, registeredUserId))
                .thenReturn(expectedUpdatedTrips);

        int result = dataMigrationService.transferTripsOwnership(anonymousUserId, registeredUserId);

        assertEquals(expectedUpdatedTrips, result);
        verify(tripRepository).transferTripOwnership(anonymousUserId, registeredUserId);
    }

    @Test
    @DisplayName("Should transfer todo lists ownership successfully")
    void transferTodoListsOwnership_Success() {
        int expectedUpdatedTodoLists = 3;
        when(todoListRepository.transferTodoListOwnership(anonymousUserId, registeredUserId))
                .thenReturn(expectedUpdatedTodoLists);

        int result = dataMigrationService.transferTodoListsOwnership(anonymousUserId, registeredUserId);

        assertEquals(expectedUpdatedTodoLists, result);
        verify(todoListRepository).transferTodoListOwnership(anonymousUserId, registeredUserId);
    }

    @Test
    @DisplayName("Should return empty list when no trips found")
    void getAnonymousUserTrips_NoTripsFound() {
        when(tripRepository.findByAnonymousCreatorId(anyLong())).thenReturn(List.of());

        List<Trip> result = dataMigrationService.getAnonymousUserTrips(anonymousUserId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(tripRepository).findByAnonymousCreatorId(anonymousUserId);
    }

    @Test
    @DisplayName("Should return empty list when no todo lists found")
    void getAnonymousUserTodoLists_NoTodoListsFound() {
        when(todoListRepository.findByAnonymousUserId(anyLong())).thenReturn(List.of());

        List<TodoList> result = dataMigrationService.getAnonymousUserTodoLists(anonymousUserId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(todoListRepository).findByAnonymousUserId(anonymousUserId);
    }

    @Test
    @DisplayName("Should return zero when no trips transferred")
    void transferTripsOwnership_NoTripsTransferred() {
        when(tripRepository.transferTripOwnership(anyLong(), anyLong())).thenReturn(0);

        int result = dataMigrationService.transferTripsOwnership(anonymousUserId, registeredUserId);

        assertEquals(0, result);
        verify(tripRepository).transferTripOwnership(anonymousUserId, registeredUserId);
    }

    @Test
    @DisplayName("Should return zero when no todo lists transferred")
    void transferTodoListsOwnership_NoTodoListsTransferred() {
        when(todoListRepository.transferTodoListOwnership(anyLong(), anyLong())).thenReturn(0);

        int result = dataMigrationService.transferTodoListsOwnership(anonymousUserId, registeredUserId);

        assertEquals(0, result);
        verify(todoListRepository).transferTodoListOwnership(anonymousUserId, registeredUserId);
    }
} 