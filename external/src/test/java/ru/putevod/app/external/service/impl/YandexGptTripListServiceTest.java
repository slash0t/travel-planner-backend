package ru.putevod.app.external.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import ru.putevod.app.external.client.PlannerClient;
import ru.putevod.app.external.dto.ai.YandexGptRequest;
import ru.putevod.app.external.exception.ServiceUnavailableException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class YandexGptTripListServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private PlannerClient plannerClient;

    @InjectMocks
    private YandexGptTripListService service;

    private final String apiKey = "test-api-key";
    private final String folderId = "test-folder-id";
    private final String apiUrl = "https://test-api-url";
    private final String maxTokens = "1000";
    private final Double temperature = 0.7;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "apiKey", apiKey);
        ReflectionTestUtils.setField(service, "folderId", folderId);
        ReflectionTestUtils.setField(service, "apiUrl", apiUrl);
        ReflectionTestUtils.setField(service, "maxTokens", maxTokens);
        ReflectionTestUtils.setField(service, "temperature", temperature);
        ReflectionTestUtils.setField(service, "objectMapper", objectMapper);
    }

    @Test
    @DisplayName("generateTripListFromPrompt - Success")
    void generateTripListFromPrompt_Success() throws Exception {
        String prompt = "Пляжный отдых в Турции";
        Map<String, Object> context = new HashMap<>();
        context.put("duration", "7 дней");
        context.put("season", "лето");

        String mockResponse = "Купальник\nСолнцезащитный крем\nПляжное полотенце";
        mockYandexGptResponse(mockResponse);

        List<String> result = service.generateTripListFromPrompt(prompt, context);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(3, result.size());
        assertEquals("Купальник", result.get(0));
        assertEquals("Солнцезащитный крем", result.get(1));
        assertEquals("Пляжное полотенце", result.get(2));

        verify(restTemplate).postForObject(eq(apiUrl), any(HttpEntity.class), eq(String.class));
    }

    @Test
    @DisplayName("generateTripListFromPrompt - Empty Prompt")
    void generateTripListFromPrompt_EmptyPrompt() {
        List<String> result = service.generateTripListFromPrompt("", new HashMap<>());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Ошибка: запрос не может быть пустым", result.get(0));
    }

    @Test
    @DisplayName("generateTripListFromPrompt - Unsafe Prompt")
    void generateTripListFromPrompt_UnsafePrompt() {
        List<String> result = service.generateTripListFromPrompt("оружие и наркотики", new HashMap<>());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Извините, запрос содержит запрещенную тематику.", result.get(0));
    }

    @Test
    @DisplayName("generateTripListFromPrompt - API Error")
    void generateTripListFromPrompt_ApiError() throws Exception {
        String prompt = "Пляжный отдых";
        when(restTemplate.postForObject(eq(apiUrl), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RuntimeException("API Error"));

        List<String> result = service.generateTripListFromPrompt(prompt, new HashMap<>());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Произошла ошибка при генерации списка. Пожалуйста, попробуйте позже.", result.get(0));
    }

    @Test
    @DisplayName("generateTripListFromTrip - Success")
    void generateTripListFromTrip_Success() throws Exception {
        Long tripId = 1L;
        Map<String, Object> context = new HashMap<>();
        Map<String, Object> tripDetails = new HashMap<>();
        tripDetails.put("title", "Поездка в Турцию");
        tripDetails.put("country", "Турция");
        tripDetails.put("city", "Анталья");
        tripDetails.put("start_date", "2024-07-01");
        tripDetails.put("end_date", "2024-07-07");

        when(plannerClient.getTripDetails(tripId)).thenReturn(tripDetails);

        String mockResponse = "Купальник\nСолнцезащитный крем\nПляжное полотенце";
        mockYandexGptResponse(mockResponse);

        List<String> result = service.generateTripListFromTrip(tripId, context);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(3, result.size());
        verify(plannerClient).getTripDetails(tripId);
    }

    @Test
    @DisplayName("generateTripListFromTrip - Trip Not Found")
    void generateTripListFromTrip_TripNotFound() {
        Long tripId = 1L;
        when(plannerClient.getTripDetails(tripId)).thenReturn(Collections.emptyMap());

        List<String> result = service.generateTripListFromTrip(tripId, new HashMap<>());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Не удалось получить информацию о поездке. Проверьте ID и попробуйте снова.", result.get(0));
    }

    @Test
    @DisplayName("generateTripListFromTemplate - Success")
    void generateTripListFromTemplate_Success() throws Exception {
        Long templateId = 1L;
        Map<String, Object> context = new HashMap<>();
        Map<String, Object> templateDetails = new HashMap<>();
        templateDetails.put("title", "Пляжный отдых");
        templateDetails.put("category", "Отдых");
        templateDetails.put("description", "Базовый список для пляжного отдыха");

        List<String> templateItems = Arrays.asList("Купальник", "Полотенце", "Крем от загара");

        when(plannerClient.getTemplateDetails(templateId)).thenReturn(templateDetails);
        when(plannerClient.getTemplateItems(templateId)).thenReturn(templateItems);

        String mockResponse = "Купальник\nСолнцезащитный крем\nПляжное полотенце";
        mockYandexGptResponse(mockResponse);

        List<String> result = service.generateTripListFromTemplate(templateId, context);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(3, result.size());
        verify(plannerClient).getTemplateDetails(templateId);
        verify(plannerClient).getTemplateItems(templateId);
    }

    @Test
    @DisplayName("generateTripListFromTemplate - Template Not Found")
    void generateTripListFromTemplate_TemplateNotFound() {
        Long templateId = 1L;
        when(plannerClient.getTemplateDetails(templateId)).thenReturn(Collections.emptyMap());

        List<String> result = service.generateTripListFromTemplate(templateId, new HashMap<>());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Не удалось получить информацию о шаблоне. Проверьте ID и попробуйте снова.", result.get(0));
    }

    @Test
    @DisplayName("isSafePrompt - Safe Prompt")
    void isSafePrompt_SafePrompt() {
        boolean result = service.isSafePrompt("Пляжный отдых в Турции");

        assertTrue(result);
    }

    @Test
    @DisplayName("isSafePrompt - Unsafe Prompt")
    void isSafePrompt_UnsafePrompt() {
        boolean result = service.isSafePrompt("оружие и наркотики");

        assertFalse(result);
    }

    @Test
    @DisplayName("isSafePrompt - Empty Prompt")
    void isSafePrompt_EmptyPrompt() {
        boolean result = service.isSafePrompt("");

        assertFalse(result);
    }

    private void mockYandexGptResponse(String response) throws Exception {
        Map<String, Object> responseMap = new HashMap<>();
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> alternatives = new ArrayList<>();
        Map<String, Object> alternative = new HashMap<>();
        Map<String, Object> message = new HashMap<>();
        
        message.put("text", response);
        alternative.put("message", message);
        alternatives.add(alternative);
        result.put("alternatives", alternatives);
        responseMap.put("result", result);

        String mockResponseJson = objectMapper.writeValueAsString(responseMap);
        when(restTemplate.postForObject(eq(apiUrl), any(HttpEntity.class), eq(String.class)))
                .thenReturn(mockResponseJson);
    }
} 