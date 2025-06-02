package ru.putevod.app.external.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.putevod.app.external.exception.ServiceUnavailableException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PlannerClientTest {

    @Mock
    private RestTemplate restTemplate;

    private PlannerClient plannerClient;
    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE_REF = new ParameterizedTypeReference<>() {
    };
    private static final ParameterizedTypeReference<List<String>> LIST_TYPE_REF = new ParameterizedTypeReference<>() {
    };

    @BeforeEach
    void setUp() {
        plannerClient = new PlannerClient(restTemplate);
        ReflectionTestUtils.setField(plannerClient, "plannerServiceUrl", "http://localhost:8082");
    }

    @Test
    @DisplayName("getTripDetails - Should return trip details on successful response")
    void getTripDetails_ShouldReturnTripDetailsOnSuccessfulResponse() {
        Long tripId = 1L;
        Map<String, Object> expectedResponse = Map.of("id", tripId, "title", "Test Trip");
        ResponseEntity<Map<String, Object>> responseEntity = new ResponseEntity<>(expectedResponse, HttpStatus.OK);

        doReturn(responseEntity).when(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(MAP_TYPE_REF)
        );

        Map<String, Object> result = plannerClient.getTripDetails(tripId);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
    }

    @Test
    @DisplayName("getTripDetails - Should return empty map on empty response")
    void getTripDetails_ShouldReturnEmptyMapOnEmptyResponse() {
        Long tripId = 1L;
        ResponseEntity<Map<String, Object>> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);

        doReturn(responseEntity).when(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(MAP_TYPE_REF)
        );

        Map<String, Object> result = plannerClient.getTripDetails(tripId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getTripDetails - Should throw ServiceUnavailableException on HttpClientErrorException.UNAUTHORIZED")
    void getTripDetails_ShouldThrowServiceUnavailableExceptionOnUnauthorized() {
        Long tripId = 1L;
        HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Unauthorized");

        doThrow(exception).when(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(MAP_TYPE_REF)
        );

        ServiceUnavailableException thrown = assertThrows(ServiceUnavailableException.class,
                () -> plannerClient.getTripDetails(tripId));
        assertTrue(thrown.getMessage().contains("Ошибка авторизации"));
    }

    @Test
    @DisplayName("getTripDetails - Should return empty map on HttpClientErrorException.NOT_FOUND")
    void getTripDetails_ShouldReturnEmptyMapOnNotFound() {
        Long tripId = 1L;
        HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.NOT_FOUND, "Not Found");

        doThrow(exception).when(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(MAP_TYPE_REF)
        );

        Map<String, Object> result = plannerClient.getTripDetails(tripId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getTripDetails - Should throw ServiceUnavailableException on ResourceAccessException")
    void getTripDetails_ShouldThrowServiceUnavailableExceptionOnResourceAccessException() {
        Long tripId = 1L;
        ResourceAccessException exception = new ResourceAccessException("Connection refused");

        doThrow(exception).when(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(MAP_TYPE_REF)
        );

        ServiceUnavailableException thrown = assertThrows(ServiceUnavailableException.class,
                () -> plannerClient.getTripDetails(tripId));
        assertTrue(thrown.getMessage().contains("Сервис планировщика недоступен"));
    }

    @Test
    @DisplayName("getTripDetails - Should throw ServiceUnavailableException on RestClientException")
    void getTripDetails_ShouldThrowServiceUnavailableExceptionOnRestClientException() {
        Long tripId = 1L;
        RestClientException exception = new RestClientException("General error");

        doThrow(exception).when(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(MAP_TYPE_REF)
        );

        ServiceUnavailableException thrown = assertThrows(ServiceUnavailableException.class,
                () -> plannerClient.getTripDetails(tripId));
        assertTrue(thrown.getMessage().contains("Ошибка при получении информации о поездке"));
    }

    @Test
    @DisplayName("getTemplateDetails - Should return template details on successful response")
    void getTemplateDetails_ShouldReturnTemplateDetailsOnSuccessfulResponse() {
        Long templateId = 1L;
        Map<String, Object> expectedResponse = Map.of("id", templateId, "title", "Test Template");
        ResponseEntity<Map<String, Object>> responseEntity = new ResponseEntity<>(expectedResponse, HttpStatus.OK);

        doReturn(responseEntity).when(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(MAP_TYPE_REF)
        );

        Map<String, Object> result = plannerClient.getTemplateDetails(templateId);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
    }

    @Test
    @DisplayName("getTemplateDetails - Should throw ServiceUnavailableException on HttpClientErrorException.UNAUTHORIZED")
    void getTemplateDetails_ShouldThrowServiceUnavailableExceptionOnUnauthorized() {
        Long templateId = 1L;
        HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Unauthorized");

        doThrow(exception).when(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(MAP_TYPE_REF)
        );

        ServiceUnavailableException thrown = assertThrows(ServiceUnavailableException.class,
                () -> plannerClient.getTemplateDetails(templateId));
        assertTrue(thrown.getMessage().contains("Ошибка авторизации"));
    }

    @Test
    @DisplayName("getTemplateDetails - Should return empty map on HttpClientErrorException.NOT_FOUND")
    void getTemplateDetails_ShouldReturnEmptyMapOnNotFound() {
        Long templateId = 1L;
        HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.NOT_FOUND, "Not Found");

        doThrow(exception).when(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(MAP_TYPE_REF)
        );

        Map<String, Object> result = plannerClient.getTemplateDetails(templateId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getTemplateDetails - Should throw ServiceUnavailableException on ResourceAccessException")
    void getTemplateDetails_ShouldThrowServiceUnavailableExceptionOnResourceAccessException() {
        Long templateId = 1L;
        ResourceAccessException exception = new ResourceAccessException("Connection refused");

        doThrow(exception).when(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(MAP_TYPE_REF)
        );

        ServiceUnavailableException thrown = assertThrows(ServiceUnavailableException.class,
                () -> plannerClient.getTemplateDetails(templateId));
        assertTrue(thrown.getMessage().contains("Сервис планировщика недоступен"));
    }

    @Test
    @DisplayName("getTemplateItems - Should return items on successful response")
    void getTemplateItems_ShouldReturnItemsOnSuccessfulResponse() {
        Long templateId = 1L;
        List<String> expectedItems = List.of("Item 1", "Item 2");
        ResponseEntity<List<String>> responseEntity = new ResponseEntity<>(expectedItems, HttpStatus.OK);

        doReturn(responseEntity).when(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(LIST_TYPE_REF)
        );

        List<String> result = plannerClient.getTemplateItems(templateId);

        assertNotNull(result);
        assertEquals(expectedItems, result);
    }

    @Test
    @DisplayName("getTemplateItems - Should return empty list on HttpClientErrorException.NOT_FOUND")
    void getTemplateItems_ShouldReturnEmptyListOnNotFound() {
        Long templateId = 1L;
        HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.NOT_FOUND, "Not Found");

        doThrow(exception).when(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(LIST_TYPE_REF)
        );

        List<String> result = plannerClient.getTemplateItems(templateId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getTemplateItems - Should throw ServiceUnavailableException on ResourceAccessException")
    void getTemplateItems_ShouldThrowServiceUnavailableExceptionOnResourceAccessException() {
        Long templateId = 1L;
        ResourceAccessException exception = new ResourceAccessException("Connection refused");

        doThrow(exception).when(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(LIST_TYPE_REF)
        );

        ServiceUnavailableException thrown = assertThrows(ServiceUnavailableException.class,
                () -> plannerClient.getTemplateItems(templateId));
        assertTrue(thrown.getMessage().contains("Сервис планировщика недоступен"));
    }

    @Test
    @DisplayName("getTemplateItems - Should throw ServiceUnavailableException on RestClientException")
    void getTemplateItems_ShouldThrowServiceUnavailableExceptionOnRestClientException() {
        Long templateId = 1L;
        RestClientException exception = new RestClientException("General error");

        doThrow(exception).when(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(LIST_TYPE_REF)
        );

        ServiceUnavailableException thrown = assertThrows(ServiceUnavailableException.class,
                () -> plannerClient.getTemplateItems(templateId));
        assertTrue(thrown.getMessage().contains("Ошибка при получении элементов шаблона"));
    }
} 