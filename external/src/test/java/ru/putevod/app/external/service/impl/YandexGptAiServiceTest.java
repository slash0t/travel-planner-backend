package ru.putevod.app.external.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.putevod.app.external.dto.ai.*;
import ru.putevod.app.external.exception.ServiceUnavailableException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class YandexGptAiServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private YandexGptAiService service;

    private static final String API_KEY = "test-api-key";
    private static final String FOLDER_ID = "test-folder-id";
    private static final String API_URL = "https://test.yandex.ru/v1/completion";
    private static final String MAX_TOKENS = "1000";
    private static final Double TEMPERATURE = 0.7;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "apiKey", API_KEY);
        ReflectionTestUtils.setField(service, "folderId", FOLDER_ID);
        ReflectionTestUtils.setField(service, "apiUrl", API_URL);
        ReflectionTestUtils.setField(service, "maxTokens", MAX_TOKENS);
        ReflectionTestUtils.setField(service, "temperature", TEMPERATURE);
    }

    @Test
    @DisplayName("generatePackingList - Empty response")
    void generatePackingList_EmptyResponse() throws Exception {
        PackingListRequest request = createSampleRequest();
        mockYandexGptResponse("");

        PackingListResponse response = service.generatePackingList(request);

        assertNotNull(response);
        assertFalse(response.getCategories().isEmpty());
        assertTrue(response.getSuggestions().contains("Не удалось сгенерировать персонализированный список"));
        assertTrue(response.getSuggestions().contains("Используется базовый шаблон"));
    }

    @Test
    @DisplayName("generatePackingList - API error")
    void generatePackingList_ApiError() {
        PackingListRequest request = createSampleRequest();
        when(restTemplate.postForObject(eq(API_URL), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RestClientException("API error"));

        PackingListResponse response = service.generatePackingList(request);

        assertNotNull(response);
        assertFalse(response.getCategories().isEmpty());
        assertTrue(response.getSuggestions().contains("Не удалось сгенерировать персонализированный список"));
        assertTrue(response.getSuggestions().contains("Используется базовый шаблон"));
    }

    @Test
    @DisplayName("generatePackingList - No API key")
    void generatePackingList_NoApiKey() {
        ReflectionTestUtils.setField(service, "apiKey", "");
        PackingListRequest request = createSampleRequest();

        PackingListResponse response = service.generatePackingList(request);

        assertNotNull(response);
        assertFalse(response.getCategories().isEmpty());
        assertTrue(response.getSuggestions().contains("Не удалось сгенерировать персонализированный список"));
        assertTrue(response.getSuggestions().contains("Используется базовый шаблон"));
    }

    @Test
    @DisplayName("getPackingListTemplateContent - Success")
    void getPackingListTemplateContent_Success() {
        PackingListTemplateContent content = service.getPackingListTemplateContent("test-template");

        assertNotNull(content);
        assertEquals("test-template", content.getId());
        assertEquals("Базовый шаблон для поездки", content.getName());
        assertEquals("Стандартный набор вещей для любой поездки", content.getDescription());
        assertEquals(12, content.getTotalItems());
        assertFalse(content.getCategories().isEmpty());
        
        PackingCategoryDto documents = findCategory(content.getCategories(), "Документы");
        assertNotNull(documents);
        assertEquals(3, documents.getItems().size());
        assertTrue(containsItem(documents, "Паспорт"));
        assertTrue(containsItem(documents, "Деньги и банковские карты"));
        assertTrue(containsItem(documents, "Билеты"));
    }

    private PackingListRequest createSampleRequest() {
        return PackingListRequest.builder()
                .destination("Москва")
                .duration(7)
                .travelType("городская")
                .season("лето")
                .activities(Arrays.asList("экскурсии", "шоппинг"))
                .participants(Collections.singletonList(
                        ParticipantDto.builder()
                                .type("взрослый")
                                .count(2)
                                .build()
                ))
                .transportation(Collections.singletonList("самолет"))
                .accommodation("отель")
                .additionalInfo("Бизнес-поездка")
                .build();
    }

    private void mockYandexGptResponse(String response) throws Exception {
        String mockResponseJson = String.format("""
            {
                "result": {
                    "alternatives": [
                        {
                            "message": {
                                "text": "%s"
                            }
                        }
                    ]
                }
            }
            """, response.replace("\n", "\\n").replace("\"", "\\\""));

        when(restTemplate.postForObject(eq(API_URL), any(HttpEntity.class), eq(String.class)))
                .thenReturn(mockResponseJson);
    }

    private PackingCategoryDto findCategory(PackingListResponse response, String name) {
        return response.getCategories().stream()
                .filter(category -> category.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    private PackingCategoryDto findCategory(List<PackingCategoryDto> categories, String name) {
        return categories.stream()
                .filter(category -> category.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    private boolean containsItem(PackingCategoryDto category, String itemName) {
        return category.getItems().stream()
                .anyMatch(item -> item.getName().equals(itemName));
    }
} 