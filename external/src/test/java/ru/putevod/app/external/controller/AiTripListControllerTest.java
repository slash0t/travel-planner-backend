package ru.putevod.app.external.controller;

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
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    @DisplayName("generate - Successful response with prompt only")
    void generate_SuccessfulResponseWithPromptOnly() {
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

        ResponseEntity<Object> response = controller.generate(prompt, null, null, duration, destination, season, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedItems, response.getBody());
    }

    @Test
    @DisplayName("generate - No parameters provided")
    void generate_NoParametersProvided() {
        ResponseEntity<Object> response = controller.generate(null, null, null, null, null, null, null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals("Ошибка", body.get("error"));
        assertEquals("Необходимо указать: prompt (для генерации с нуля), tripId (для генерации по поездке), или templateId (для генерации по шаблону)", body.get("message"));
    }

    @Test
    @DisplayName("generate - Empty prompt")
    void generate_EmptyPrompt() {
        ResponseEntity<Object> response = controller.generate("", null, null, null, null, null, null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals("Ошибка", body.get("error"));
        assertEquals("Необходимо указать: prompt (для генерации с нуля), tripId (для генерации по поездке), или templateId (для генерации по шаблону)", body.get("message"));
    }

    @Test
    @DisplayName("generate - Both tripId and templateId provided")
    void generate_BothTripIdAndTemplateIdProvided() {
        ResponseEntity<Object> response = controller.generate(null, 1L, 1L, null, null, null, null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals("Ошибка", body.get("error"));
        assertEquals("Нельзя одновременно указывать tripId и templateId", body.get("message"));
    }

    @Test
    @DisplayName("generate - Unsafe prompt")
    void generate_UnsafePrompt() {
        String prompt = "поездка с оружием";
        when(aiTripListService.isSafePrompt(prompt)).thenReturn(false);

        ResponseEntity<Object> response = controller.generate(prompt, null, null, null, null, null, null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals("Ошибка безопасности", body.get("error"));
        assertEquals("Запрос содержит запрещенную тематику", body.get("message"));
    }

    @Test
    @DisplayName("generate - Unsafe additional prompt")
    void generate_UnsafeAdditionalPrompt() {
        String prompt = "Поездка на пляж";
        String additionalPrompt = "добавить оружие";
        when(aiTripListService.isSafePrompt(prompt)).thenReturn(true);
        when(aiTripListService.isSafePrompt(additionalPrompt)).thenReturn(false);

        ResponseEntity<Object> response = controller.generate(prompt, null, null, null, null, null, additionalPrompt);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals("Ошибка безопасности", body.get("error"));
        assertEquals("Дополнительный запрос содержит запрещенную тематику", body.get("message"));
    }

    @Test
    @DisplayName("generate - Service unavailable")
    void generate_ServiceUnavailable() {
        String prompt = "Поездка на пляж";
        when(aiTripListService.isSafePrompt(prompt)).thenReturn(true);
        when(aiTripListService.generateTripListFromPrompt(eq(prompt), any()))
                .thenThrow(new ServiceUnavailableException("Service is down"));

        ResponseEntity<Object> response = controller.generate(prompt, null, null, null, null, null, null);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals("Сервис недоступен", body.get("error"));
        assertEquals("Service is down", body.get("message"));
    }

    @Test
    @DisplayName("generate - Successful response with tripId")
    void generate_SuccessfulResponseWithTripId() {
        Long tripId = 1L;
        String additionalPrompt = "Добавить спортивное снаряжение";
        List<String> expectedItems = Arrays.asList(
                "Паспорт и документы",
                "Деньги и банковские карты",
                "Спортивное снаряжение"
        );

        when(aiTripListService.isSafePrompt(additionalPrompt)).thenReturn(true);
        when(aiTripListService.generateTripListFromTrip(eq(tripId), any())).thenReturn(expectedItems);

        ResponseEntity<Object> response = controller.generate(null, tripId, null, null, null, null, additionalPrompt);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedItems, response.getBody());
    }

    @Test
    @DisplayName("generate - Invalid trip ID")
    void generate_InvalidTripId() {
        ResponseEntity<Object> response = controller.generate(null, 0L, null, null, null, null, null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals("Ошибка", body.get("error"));
        assertEquals("Необходимо указать: prompt (для генерации с нуля), tripId (для генерации по поездке), или templateId (для генерации по шаблону)", body.get("message"));
    }

    @Test
    @DisplayName("generate - Trip not found")
    void generate_TripNotFound() {
        Long tripId = 1L;
        when(aiTripListService.generateTripListFromTrip(eq(tripId), any()))
                .thenReturn(List.of("Не удалось получить информацию о поездке. Проверьте ID и попробуйте снова."));

        ResponseEntity<Object> response = controller.generate(null, tripId, null, null, null, null, null);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals("Поездка не найдена", body.get("error"));
    }

    @Test
    @DisplayName("generate - Successful response with templateId")
    void generate_SuccessfulResponseWithTemplateId() {
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

        ResponseEntity<Object> response = controller.generate(
                null, null, templateId, duration, destination, season, additionalPrompt);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedItems, response.getBody());
    }

    @Test
    @DisplayName("generate - Invalid template ID")
    void generate_InvalidTemplateId() {
        ResponseEntity<Object> response = controller.generate(
                null, null, 0L, null, null, null, null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals("Ошибка", body.get("error"));
        assertEquals("Необходимо указать: prompt (для генерации с нуля), tripId (для генерации по поездке), или templateId (для генерации по шаблону)", body.get("message"));
    }

    @Test
    @DisplayName("generate - Template not found")
    void generate_TemplateNotFound() {
        Long templateId = 1L;
        when(aiTripListService.generateTripListFromTemplate(eq(templateId), any()))
                .thenReturn(List.of("Не удалось получить информацию о шаблоне. Проверьте ID и попробуйте снова."));

        ResponseEntity<Object> response = controller.generate(
                null, null, templateId, null, null, null, null);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals("Шаблон не найден", body.get("error"));
    }

    @Test
    @DisplayName("generate - Error generation response")
    void generate_ErrorGenerationResponse() {
        String prompt = "Поездка на пляж";
        when(aiTripListService.isSafePrompt(prompt)).thenReturn(true);
        when(aiTripListService.generateTripListFromPrompt(eq(prompt), any()))
                .thenReturn(List.of("Ошибка: Не удалось сгенерировать список"));

        ResponseEntity<Object> response = controller.generate(prompt, null, null, null, null, null, null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals("Ошибка генерации", body.get("error"));
        assertEquals("Ошибка: Не удалось сгенерировать список", body.get("message"));
    }

    @Test
    @DisplayName("generate - Generic exception")
    void generate_GenericException() {
        String prompt = "Поездка на пляж";
        when(aiTripListService.isSafePrompt(prompt)).thenReturn(true);
        when(aiTripListService.generateTripListFromPrompt(eq(prompt), any()))
                .thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<Object> response = controller.generate(prompt, null, null, null, null, null, null);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals("Внутренняя ошибка сервера", body.get("error"));
        assertEquals("Произошла ошибка при обработке запроса", body.get("message"));
    }

    @Test
    @DisplayName("generate - Successful response with tripId and prompt")
    void generate_SuccessfulResponseWithTripIdAndPrompt() {
        Long tripId = 1L;
        String prompt = "Дополнительный запрос";
        List<String> expectedItems = Arrays.asList(
                "Паспорт и документы",
                "Деньги и банковские карты"
        );

        when(aiTripListService.isSafePrompt(prompt)).thenReturn(true);
        when(aiTripListService.generateTripListFromTrip(eq(tripId), any())).thenReturn(expectedItems);

        ResponseEntity<Object> response = controller.generate(prompt, tripId, null, null, null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedItems, response.getBody());
    }

    @Test
    @DisplayName("generate - Successful response with templateId and prompt")
    void generate_SuccessfulResponseWithTemplateIdAndPrompt() {
        Long templateId = 1L;
        String prompt = "Дополнительный запрос";
        List<String> expectedItems = Arrays.asList(
                "Паспорт и документы",
                "Деньги и банковские карты"
        );

        when(aiTripListService.isSafePrompt(prompt)).thenReturn(true);
        when(aiTripListService.generateTripListFromTemplate(eq(templateId), any())).thenReturn(expectedItems);

        ResponseEntity<Object> response = controller.generate(prompt, null, templateId, null, null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedItems, response.getBody());
    }
} 