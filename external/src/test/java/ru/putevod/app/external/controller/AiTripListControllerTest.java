package ru.putevod.app.external.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.putevod.app.external.exception.ServiceUnavailableException;
import ru.putevod.app.external.service.AiTripListService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiTripListControllerTest {

    @Mock
    private AiTripListService aiTripListService;

    @InjectMocks
    private AiTripListController controller;

    @Test
    @DisplayName("generateTripList - Successful response")
    void generateTripList_SuccessfulResponse() {
        String prompt = "Поездка на пляж";
        Integer duration = 7;
        String destination = "Сочи";
        String season = "лето";
        List<String> expectedItems = Arrays.asList(
            "Плавательные шорты/купальник",
            "Солнцезащитный крем",
            "Пляжное полотенце"
        );

        when(aiTripListService.isSafePrompt(prompt)).thenReturn(true);
        when(aiTripListService.generateTripListFromPrompt(eq(prompt), any())).thenReturn(expectedItems);

        ResponseEntity<Object> response = controller.generateTripList(prompt, duration, destination, season);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedItems, response.getBody());
    }

    @Test
    @DisplayName("generateTripList - Empty prompt")
    void generateTripList_EmptyPrompt() {
        ResponseEntity<Object> response = controller.generateTripList("", null, null, null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals("Ошибка", body.get("error"));
        assertEquals("Запрос не может быть пустым", body.get("message"));
    }

    @Test
    @DisplayName("generateTripList - Unsafe prompt")
    void generateTripList_UnsafePrompt() {
        String prompt = "поездка с оружием";
        when(aiTripListService.isSafePrompt(prompt)).thenReturn(false);

        ResponseEntity<Object> response = controller.generateTripList(prompt, null, null, null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals("Ошибка безопасности", body.get("error"));
        assertEquals("Запрос содержит запрещенную тематику", body.get("message"));
    }

    @Test
    @DisplayName("generateTripList - Service unavailable")
    void generateTripList_ServiceUnavailable() {
        String prompt = "Поездка на пляж";
        when(aiTripListService.isSafePrompt(prompt)).thenReturn(true);
        when(aiTripListService.generateTripListFromPrompt(eq(prompt), any()))
                .thenThrow(new ServiceUnavailableException("Service is down"));

        ResponseEntity<Object> response = controller.generateTripList(prompt, null, null, null);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals("Сервис недоступен", body.get("error"));
        assertEquals("Service is down", body.get("message"));
    }

    @Test
    @DisplayName("generateTripListFromTrip - Successful response")
    void generateTripListFromTrip_SuccessfulResponse() {
        Long tripId = 1L;
        String additionalPrompt = "Добавить спортивное снаряжение";
        List<String> expectedItems = Arrays.asList(
            "Паспорт и документы",
            "Деньги и банковские карты",
            "Спортивное снаряжение"
        );

        when(aiTripListService.isSafePrompt(additionalPrompt)).thenReturn(true);
        when(aiTripListService.generateTripListFromTrip(eq(tripId), any())).thenReturn(expectedItems);

        ResponseEntity<Object> response = controller.generateTripListFromTrip(tripId, additionalPrompt);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedItems, response.getBody());
    }

    @Test
    @DisplayName("generateTripListFromTrip - Invalid trip ID")
    void generateTripListFromTrip_InvalidTripId() {
        ResponseEntity<Object> response = controller.generateTripListFromTrip(0L, null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals("Ошибка", body.get("error"));
        assertEquals("ID поездки должен быть положительным числом", body.get("message"));
    }

    @Test
    @DisplayName("generateTripListFromTrip - Trip not found")
    void generateTripListFromTrip_TripNotFound() {
        Long tripId = 1L;
        when(aiTripListService.generateTripListFromTrip(eq(tripId), any()))
                .thenReturn(List.of("Не удалось получить информацию о поездке. Проверьте ID и попробуйте снова."));

        ResponseEntity<Object> response = controller.generateTripListFromTrip(tripId, null);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals("Поездка не найдена", body.get("error"));
    }

    @Test
    @DisplayName("generateTripListFromTemplate - Successful response")
    void generateTripListFromTemplate_SuccessfulResponse() {
        Long templateId = 1L;
        Integer duration = 7;
        String destination = "Париж";
        String season = "весна";
        String additionalPrompt = "Добавить фотоаппарат";
        List<String> expectedItems = Arrays.asList(
            "Паспорт и документы",
            "Деньги и банковские карты",
            "Фотоаппарат"
        );

        when(aiTripListService.isSafePrompt(additionalPrompt)).thenReturn(true);
        when(aiTripListService.generateTripListFromTemplate(eq(templateId), any())).thenReturn(expectedItems);

        ResponseEntity<Object> response = controller.generateTripListFromTemplate(
            templateId, duration, destination, season, additionalPrompt);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedItems, response.getBody());
    }

    @Test
    @DisplayName("generateTripListFromTemplate - Invalid template ID")
    void generateTripListFromTemplate_InvalidTemplateId() {
        ResponseEntity<Object> response = controller.generateTripListFromTemplate(
            0L, null, null, null, null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals("Ошибка", body.get("error"));
        assertEquals("ID шаблона должен быть положительным числом", body.get("message"));
    }

    @Test
    @DisplayName("generateTripListFromTemplate - Template not found")
    void generateTripListFromTemplate_TemplateNotFound() {
        Long templateId = 1L;
        when(aiTripListService.generateTripListFromTemplate(eq(templateId), any()))
                .thenReturn(List.of("Не удалось получить информацию о шаблоне. Проверьте ID и попробуйте снова."));

        ResponseEntity<Object> response = controller.generateTripListFromTemplate(
            templateId, null, null, null, null);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals("Шаблон не найден", body.get("error"));
    }
} 