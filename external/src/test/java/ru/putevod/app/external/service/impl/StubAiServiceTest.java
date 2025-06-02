package ru.putevod.app.external.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.putevod.app.external.dto.ai.*;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class StubAiServiceTest {

    @InjectMocks
    private StubAiService service;

    @Test
    @DisplayName("generatePackingList - Success")
    void generatePackingList_Success() {
        PackingListRequest request = PackingListRequest.builder()
                .destination("Paris")
                .duration(7)
                .season("summer")
                .travelType("city")
                .participants(Collections.singletonList(new ParticipantDto("adult", 1)))
                .build();

        PackingListResponse response = service.generatePackingList(request);

        assertNotNull(response);
        assertNotNull(response.getCategories());
        assertFalse(response.getCategories().isEmpty());
        assertNotNull(response.getSuggestions());
        assertFalse(response.getSuggestions().isEmpty());
        assertEquals(12, response.getTotalItems());

        List<PackingCategoryDto> categories = response.getCategories();
        assertEquals(3, categories.size());

        PackingCategoryDto documentsCategory = categories.stream()
                .filter(c -> c.getName().equals("Документы"))
                .findFirst()
                .orElse(null);
        assertNotNull(documentsCategory);
        assertEquals(3, documentsCategory.getItems().size());
        assertTrue(documentsCategory.getItems().stream()
                .anyMatch(item -> item.getName().equals("Паспорт")));
        assertTrue(documentsCategory.getItems().stream()
                .anyMatch(item -> item.getName().equals("Деньги и банковские карты")));
        assertTrue(documentsCategory.getItems().stream()
                .anyMatch(item -> item.getName().equals("Билеты")));

        PackingCategoryDto clothesCategory = categories.stream()
                .filter(c -> c.getName().equals("Одежда"))
                .findFirst()
                .orElse(null);
        assertNotNull(clothesCategory);
        assertEquals(4, clothesCategory.getItems().size());
        assertTrue(clothesCategory.getItems().stream()
                .anyMatch(item -> item.getName().equals("Футболки")));
        assertTrue(clothesCategory.getItems().stream()
                .anyMatch(item -> item.getName().equals("Брюки/шорты")));

        PackingCategoryDto hygieneCategory = categories.stream()
                .filter(c -> c.getName().equals("Гигиена"))
                .findFirst()
                .orElse(null);
        assertNotNull(hygieneCategory);
        assertEquals(5, hygieneCategory.getItems().size());
        assertTrue(hygieneCategory.getItems().stream()
                .anyMatch(item -> item.getName().equals("Зубная щетка и паста")));
        assertTrue(hygieneCategory.getItems().stream()
                .anyMatch(item -> item.getName().equals("Шампунь")));
    }

    @Test
    @DisplayName("getPackingListTemplates - Success")
    void getPackingListTemplates_Success() {
        PackingListTemplatesResponse response = service.getPackingListTemplates();

        assertNotNull(response);
        assertNotNull(response.getTemplates());
        assertEquals(3, response.getTemplates().size());

        PackingListTemplate beachTemplate = response.getTemplates().stream()
                .filter(t -> t.getId().equals("beach-trip"))
                .findFirst()
                .orElse(null);
        assertNotNull(beachTemplate);
        assertEquals("Пляжный отдых", beachTemplate.getName());
        assertEquals("Базовый список вещей для пляжного отпуска", beachTemplate.getDescription());
        assertEquals("Пляжный отдых", beachTemplate.getCategory());
        assertEquals("https://example.com/images/beach.jpg", beachTemplate.getImageUrl());
        assertEquals(20, beachTemplate.getItemCount());

        PackingListTemplate cityTemplate = response.getTemplates().stream()
                .filter(t -> t.getId().equals("city-trip"))
                .findFirst()
                .orElse(null);
        assertNotNull(cityTemplate);
        assertEquals("Городской туризм", cityTemplate.getName());
        assertEquals("Необходимые вещи для исследования городов", cityTemplate.getDescription());
        assertEquals("Городской туризм", cityTemplate.getCategory());
        assertEquals("https://example.com/images/city.jpg", cityTemplate.getImageUrl());
        assertEquals(18, cityTemplate.getItemCount());

        PackingListTemplate hikingTemplate = response.getTemplates().stream()
                .filter(t -> t.getId().equals("hiking"))
                .findFirst()
                .orElse(null);
        assertNotNull(hikingTemplate);
        assertEquals("Поход в горы", hikingTemplate.getName());
        assertEquals("Список для любителей активного отдыха в горах", hikingTemplate.getDescription());
        assertEquals("Активный отдых", hikingTemplate.getCategory());
        assertEquals("https://example.com/images/hiking.jpg", hikingTemplate.getImageUrl());
        assertEquals(25, hikingTemplate.getItemCount());
    }

    @Test
    @DisplayName("getPackingListTemplateContent - Success")
    void getPackingListTemplateContent_Success() {
        String templateId = "beach-trip";

        PackingListTemplateContent response = service.getPackingListTemplateContent(templateId);

        assertNotNull(response);
        assertEquals(templateId, response.getId());
        assertEquals("Базовый шаблон для поездки", response.getName());
        assertEquals("Стандартный набор вещей для любой поездки", response.getDescription());
        assertEquals(12, response.getTotalItems());

        List<PackingCategoryDto> categories = response.getCategories();
        assertNotNull(categories);
        assertEquals(3, categories.size());

        PackingCategoryDto documentsCategory = categories.stream()
                .filter(c -> c.getName().equals("Документы"))
                .findFirst()
                .orElse(null);
        assertNotNull(documentsCategory);
        assertEquals(3, documentsCategory.getItems().size());
        assertTrue(documentsCategory.getItems().stream()
                .anyMatch(item -> item.getName().equals("Паспорт")));
        assertTrue(documentsCategory.getItems().stream()
                .anyMatch(item -> item.getName().equals("Деньги и банковские карты")));
        assertTrue(documentsCategory.getItems().stream()
                .anyMatch(item -> item.getName().equals("Билеты")));

        PackingCategoryDto clothesCategory = categories.stream()
                .filter(c -> c.getName().equals("Одежда"))
                .findFirst()
                .orElse(null);
        assertNotNull(clothesCategory);
        assertEquals(4, clothesCategory.getItems().size());
        assertTrue(clothesCategory.getItems().stream()
                .anyMatch(item -> item.getName().equals("Футболки")));
        assertTrue(clothesCategory.getItems().stream()
                .anyMatch(item -> item.getName().equals("Брюки/шорты")));

        PackingCategoryDto hygieneCategory = categories.stream()
                .filter(c -> c.getName().equals("Гигиена"))
                .findFirst()
                .orElse(null);
        assertNotNull(hygieneCategory);
        assertEquals(5, hygieneCategory.getItems().size());
        assertTrue(hygieneCategory.getItems().stream()
                .anyMatch(item -> item.getName().equals("Зубная щетка и паста")));
        assertTrue(hygieneCategory.getItems().stream()
                .anyMatch(item -> item.getName().equals("Шампунь")));
    }
} 