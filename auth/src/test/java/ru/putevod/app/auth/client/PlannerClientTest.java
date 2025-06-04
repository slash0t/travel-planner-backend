package ru.putevod.app.auth.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import ru.putevod.app.auth.config.AppProperties;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlannerClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private AppProperties appProperties;

    @Mock
    private AppProperties.Services services;

    @InjectMocks
    private PlannerClient plannerClient;

    private static final String TEST_PLANNER_URL = "http://localhost:8082";
    private static final String TEST_AUTH_TOKEN = "test-auth-token";
    private static final Long TEST_ANONYMOUS_USER_ID = 1L;
    private static final Long TEST_REGISTERED_USER_ID = 2L;

    @BeforeEach
    void setUp() {
        when(appProperties.getServices()).thenReturn(services);
        when(services.getPlannerUrl()).thenReturn(TEST_PLANNER_URL);
        when(appProperties.getAuthToken()).thenReturn(TEST_AUTH_TOKEN);
    }

    @Test
    void migrateAllData_Success() {
        Map<String, Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status", "success");
        expectedResponse.put("migratedTrips", 5);
        expectedResponse.put("migratedTodoLists", 3);

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(expectedResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(responseEntity);

        Map<String, Object> result = plannerClient.migrateAllData(TEST_ANONYMOUS_USER_ID, TEST_REGISTERED_USER_ID);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
        assertEquals("success", result.get("status"));
        assertEquals(5, result.get("migratedTrips"));
        assertEquals(3, result.get("migratedTodoLists"));

        verify(restTemplate).exchange(
                eq(TEST_PLANNER_URL + "/migration/complete?anonymousUserId=" + TEST_ANONYMOUS_USER_ID + "&registeredUserId=" + TEST_REGISTERED_USER_ID),
                eq(HttpMethod.POST),
                argThat(entity -> {
                    HttpHeaders headers = entity.getHeaders();
                    return headers.getFirst("X-Service-Token").equals(TEST_AUTH_TOKEN);
                }),
                eq(Map.class)
        );
    }

    @Test
    void migrateAllData_ServiceError() {
        RuntimeException expectedException = new RuntimeException("Service unavailable");
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenThrow(expectedException);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> plannerClient.migrateAllData(TEST_ANONYMOUS_USER_ID, TEST_REGISTERED_USER_ID));
        assertEquals("Ошибка при миграции данных: Service unavailable", exception.getMessage());
        assertEquals(expectedException, exception.getCause());

        verify(restTemplate).exchange(
                eq(TEST_PLANNER_URL + "/migration/complete?anonymousUserId=" + TEST_ANONYMOUS_USER_ID + "&registeredUserId=" + TEST_REGISTERED_USER_ID),
                eq(HttpMethod.POST),
                argThat(entity -> {
                    HttpHeaders headers = entity.getHeaders();
                    return headers.getFirst("X-Service-Token").equals(TEST_AUTH_TOKEN);
                }),
                eq(Map.class)
        );
    }

    @Test
    void migrateAllData_NullResponse() {
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(responseEntity);

        Map<String, Object> result = plannerClient.migrateAllData(TEST_ANONYMOUS_USER_ID, TEST_REGISTERED_USER_ID);

        assertNull(result);

        verify(restTemplate).exchange(
                eq(TEST_PLANNER_URL + "/migration/complete?anonymousUserId=" + TEST_ANONYMOUS_USER_ID + "&registeredUserId=" + TEST_REGISTERED_USER_ID),
                eq(HttpMethod.POST),
                argThat(entity -> {
                    HttpHeaders headers = entity.getHeaders();
                    return headers.getFirst("X-Service-Token").equals(TEST_AUTH_TOKEN);
                }),
                eq(Map.class)
        );
    }

    @Test
    void transferTripsOwnership_Success() {
        Map<String, Object> expectedResponse = new HashMap<>();
        expectedResponse.put("message", "Путешествия успешно перенесены");
        expectedResponse.put("transferredTrips", 3);
        expectedResponse.put("anonymousUserId", TEST_ANONYMOUS_USER_ID);
        expectedResponse.put("registeredUserId", TEST_REGISTERED_USER_ID);

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(expectedResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(responseEntity);

        Map<String, Object> result = plannerClient.transferTripsOwnership(TEST_ANONYMOUS_USER_ID, TEST_REGISTERED_USER_ID);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
        assertEquals("Путешествия успешно перенесены", result.get("message"));
        assertEquals(3, result.get("transferredTrips"));
        assertEquals(TEST_ANONYMOUS_USER_ID, result.get("anonymousUserId"));
        assertEquals(TEST_REGISTERED_USER_ID, result.get("registeredUserId"));

        verify(restTemplate).exchange(
                eq(TEST_PLANNER_URL + "/migration/trips/transfer?anonymousUserId=" + TEST_ANONYMOUS_USER_ID + "&registeredUserId=" + TEST_REGISTERED_USER_ID),
                eq(HttpMethod.POST),
                argThat(entity -> {
                    HttpHeaders headers = entity.getHeaders();
                    return headers.getFirst("X-Service-Token").equals(TEST_AUTH_TOKEN);
                }),
                eq(Map.class)
        );
    }

    @Test
    void transferTripsOwnership_ServiceError() {
        RuntimeException expectedException = new RuntimeException("Service unavailable");
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenThrow(expectedException);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> plannerClient.transferTripsOwnership(TEST_ANONYMOUS_USER_ID, TEST_REGISTERED_USER_ID));
        assertEquals("Ошибка при переносе путешествий: Service unavailable", exception.getMessage());
        assertEquals(expectedException, exception.getCause());

        verify(restTemplate).exchange(
                eq(TEST_PLANNER_URL + "/migration/trips/transfer?anonymousUserId=" + TEST_ANONYMOUS_USER_ID + "&registeredUserId=" + TEST_REGISTERED_USER_ID),
                eq(HttpMethod.POST),
                argThat(entity -> {
                    HttpHeaders headers = entity.getHeaders();
                    return headers.getFirst("X-Service-Token").equals(TEST_AUTH_TOKEN);
                }),
                eq(Map.class)
        );
    }

    @Test
    void transferTripsOwnership_NullResponse() {
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(responseEntity);

        Map<String, Object> result = plannerClient.transferTripsOwnership(TEST_ANONYMOUS_USER_ID, TEST_REGISTERED_USER_ID);

        assertNull(result);

        verify(restTemplate).exchange(
                eq(TEST_PLANNER_URL + "/migration/trips/transfer?anonymousUserId=" + TEST_ANONYMOUS_USER_ID + "&registeredUserId=" + TEST_REGISTERED_USER_ID),
                eq(HttpMethod.POST),
                argThat(entity -> {
                    HttpHeaders headers = entity.getHeaders();
                    return headers.getFirst("X-Service-Token").equals(TEST_AUTH_TOKEN);
                }),
                eq(Map.class)
        );
    }

    @Test
    void transferTodoListsOwnership_Success() {
        Map<String, Object> expectedResponse = new HashMap<>();
        expectedResponse.put("message", "TODO листы успешно перенесены");
        expectedResponse.put("transferredTodoLists", 2);
        expectedResponse.put("anonymousUserId", TEST_ANONYMOUS_USER_ID);
        expectedResponse.put("registeredUserId", TEST_REGISTERED_USER_ID);

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(expectedResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(responseEntity);

        Map<String, Object> result = plannerClient.transferTodoListsOwnership(TEST_ANONYMOUS_USER_ID, TEST_REGISTERED_USER_ID);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
        assertEquals("TODO листы успешно перенесены", result.get("message"));
        assertEquals(2, result.get("transferredTodoLists"));
        assertEquals(TEST_ANONYMOUS_USER_ID, result.get("anonymousUserId"));
        assertEquals(TEST_REGISTERED_USER_ID, result.get("registeredUserId"));

        verify(restTemplate).exchange(
                eq(TEST_PLANNER_URL + "/migration/todo-lists/transfer?anonymousUserId=" + TEST_ANONYMOUS_USER_ID + "&registeredUserId=" + TEST_REGISTERED_USER_ID),
                eq(HttpMethod.POST),
                argThat(entity -> {
                    HttpHeaders headers = entity.getHeaders();
                    return headers.getFirst("X-Service-Token").equals(TEST_AUTH_TOKEN);
                }),
                eq(Map.class)
        );
    }

    @Test
    void transferTodoListsOwnership_ServiceError() {
        RuntimeException expectedException = new RuntimeException("Service unavailable");
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenThrow(expectedException);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> plannerClient.transferTodoListsOwnership(TEST_ANONYMOUS_USER_ID, TEST_REGISTERED_USER_ID));
        assertEquals("Ошибка при переносе TODO листов: Service unavailable", exception.getMessage());
        assertEquals(expectedException, exception.getCause());

        verify(restTemplate).exchange(
                eq(TEST_PLANNER_URL + "/migration/todo-lists/transfer?anonymousUserId=" + TEST_ANONYMOUS_USER_ID + "&registeredUserId=" + TEST_REGISTERED_USER_ID),
                eq(HttpMethod.POST),
                argThat(entity -> {
                    HttpHeaders headers = entity.getHeaders();
                    return headers.getFirst("X-Service-Token").equals(TEST_AUTH_TOKEN);
                }),
                eq(Map.class)
        );
    }

    @Test
    void transferTodoListsOwnership_NullResponse() {
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(responseEntity);

        Map<String, Object> result = plannerClient.transferTodoListsOwnership(TEST_ANONYMOUS_USER_ID, TEST_REGISTERED_USER_ID);

        assertNull(result);

        verify(restTemplate).exchange(
                eq(TEST_PLANNER_URL + "/migration/todo-lists/transfer?anonymousUserId=" + TEST_ANONYMOUS_USER_ID + "&registeredUserId=" + TEST_REGISTERED_USER_ID),
                eq(HttpMethod.POST),
                argThat(entity -> {
                    HttpHeaders headers = entity.getHeaders();
                    return headers.getFirst("X-Service-Token").equals(TEST_AUTH_TOKEN);
                }),
                eq(Map.class)
        );
    }
} 