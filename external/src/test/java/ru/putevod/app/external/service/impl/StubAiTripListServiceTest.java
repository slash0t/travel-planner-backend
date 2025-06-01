package ru.putevod.app.external.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class StubAiTripListServiceTest {

    @InjectMocks
    private StubAiTripListService service;

    @Test
    @DisplayName("generateTripListFromPrompt - Beach trip")
    void generateTripListFromPrompt_BeachTrip() {
        String prompt = "Поездка на пляж";
        Map<String, Object> context = new HashMap<>();
        context.put("duration", 7);
        context.put("season", "summer");

        List<String> items = service.generateTripListFromPrompt(prompt, context);

        assertNotNull(items);
        assertEquals(10, items.size());
        assertTrue(items.contains("Плавательные шорты/купальник"));
        assertTrue(items.contains("Солнцезащитный крем"));
        assertTrue(items.contains("Пляжное полотенце"));
        assertTrue(items.contains("Солнцезащитные очки"));
        assertTrue(items.contains("Шляпа от солнца"));
        assertTrue(items.contains("Тапочки для пляжа"));
        assertTrue(items.contains("Надувной матрас/круг"));
        assertTrue(items.contains("Пляжная сумка"));
        assertTrue(items.contains("Антисептик для рук"));
        assertTrue(items.contains("Книга/журнал для чтения"));
    }

    @Test
    @DisplayName("generateTripListFromPrompt - City trip")
    void generateTripListFromPrompt_CityTrip() {
        String prompt = "Городской туризм";
        Map<String, Object> context = new HashMap<>();
        context.put("duration", 5);
        context.put("season", "spring");

        List<String> items = service.generateTripListFromPrompt(prompt, context);

        assertNotNull(items);
        assertEquals(10, items.size());
        assertTrue(items.contains("Удобная обувь для длительных прогулок"));
        assertTrue(items.contains("Карта города"));
        assertTrue(items.contains("Путеводитель"));
        assertTrue(items.contains("Фотоаппарат"));
        assertTrue(items.contains("Внешний аккумулятор для телефона"));
        assertTrue(items.contains("Бутылка для воды"));
        assertTrue(items.contains("Солнцезащитные очки"));
        assertTrue(items.contains("Легкий рюкзак для дневных прогулок"));
        assertTrue(items.contains("Зонтик или дождевик"));
        assertTrue(items.contains("Деньги/карты в разных местах (для безопасности)"));
    }

    @Test
    @DisplayName("generateTripListFromPrompt - Default list")
    void generateTripListFromPrompt_DefaultList() {
        String prompt = "Обычная поездка";
        Map<String, Object> context = new HashMap<>();
        context.put("duration", 3);
        context.put("season", "winter");

        List<String> items = service.generateTripListFromPrompt(prompt, context);

        assertNotNull(items);
        assertEquals(10, items.size());
        assertTrue(items.contains("Паспорт и документы"));
        assertTrue(items.contains("Деньги и банковские карты"));
        assertTrue(items.contains("Телефон и зарядное устройство"));
        assertTrue(items.contains("Одежда по сезону"));
        assertTrue(items.contains("Нижнее белье и носки"));
        assertTrue(items.contains("Предметы личной гигиены"));
        assertTrue(items.contains("Аптечка с необходимыми лекарствами"));
        assertTrue(items.contains("Страховой полис"));
        assertTrue(items.contains("Билеты (электронные копии)"));
        assertTrue(items.contains("Бронь отеля (распечатка)"));
    }

    @Test
    @DisplayName("generateTripListFromTrip - Success")
    void generateTripListFromTrip_Success() {
        Long tripId = 1L;
        Map<String, Object> context = new HashMap<>();
        context.put("duration", 5);
        context.put("season", "summer");

        List<String> items = service.generateTripListFromTrip(tripId, context);

        assertNotNull(items);
        assertEquals(10, items.size());
        assertTrue(items.contains("Паспорт и документы"));
        assertTrue(items.contains("Деньги и банковские карты"));
        assertTrue(items.contains("Телефон и зарядное устройство"));
        assertTrue(items.contains("Одежда по сезону (проверьте прогноз погоды)"));
        assertTrue(items.contains("Нижнее белье и носки"));
        assertTrue(items.contains("Предметы личной гигиены"));
        assertTrue(items.contains("Аптечка с необходимыми лекарствами"));
        assertTrue(items.contains("Страховой полис"));
        assertTrue(items.contains("Билеты (электронные копии)"));
        assertTrue(items.contains("Бронь отеля (распечатка)"));
    }

    @Test
    @DisplayName("generateTripListFromTemplate - Beach template")
    void generateTripListFromTemplate_BeachTemplate() {
        Long templateId = 1L;
        Map<String, Object> context = new HashMap<>();
        context.put("duration", 7);
        context.put("season", "summer");

        List<String> items = service.generateTripListFromTemplate(templateId, context);

        assertNotNull(items);
        assertEquals(10, items.size());
        assertTrue(items.contains("Плавательные шорты/купальник"));
        assertTrue(items.contains("Солнцезащитный крем"));
        assertTrue(items.contains("Пляжное полотенце"));
        assertTrue(items.contains("Солнцезащитные очки"));
        assertTrue(items.contains("Шляпа от солнца"));
        assertTrue(items.contains("Тапочки для пляжа"));
        assertTrue(items.contains("Надувной матрас/круг"));
        assertTrue(items.contains("Пляжная сумка"));
        assertTrue(items.contains("Антисептик для рук"));
        assertTrue(items.contains("Книга/журнал для чтения"));
    }

    @Test
    @DisplayName("generateTripListFromTemplate - Hiking template")
    void generateTripListFromTemplate_HikingTemplate() {
        Long templateId = 2L;
        Map<String, Object> context = new HashMap<>();
        context.put("duration", 3);
        context.put("season", "summer");

        List<String> items = service.generateTripListFromTemplate(templateId, context);

        assertNotNull(items);
        assertEquals(10, items.size());
        assertTrue(items.contains("Треккинговые ботинки"));
        assertTrue(items.contains("Рюкзак"));
        assertTrue(items.contains("Палатка"));
        assertTrue(items.contains("Спальный мешок"));
        assertTrue(items.contains("Карта местности"));
        assertTrue(items.contains("Компас или GPS"));
        assertTrue(items.contains("Фонарик и запасные батарейки"));
        assertTrue(items.contains("Нож или мультитул"));
        assertTrue(items.contains("Аптечка первой помощи"));
        assertTrue(items.contains("Термос"));
    }

    @Test
    @DisplayName("generateTripListFromTemplate - Default template")
    void generateTripListFromTemplate_DefaultTemplate() {
        Long templateId = 3L;
        Map<String, Object> context = new HashMap<>();
        context.put("duration", 5);
        context.put("season", "winter");

        List<String> items = service.generateTripListFromTemplate(templateId, context);

        assertNotNull(items);
        assertEquals(10, items.size());
        assertTrue(items.contains("Паспорт и документы"));
        assertTrue(items.contains("Деньги и банковские карты"));
        assertTrue(items.contains("Телефон и зарядное устройство"));
        assertTrue(items.contains("Одежда по сезону"));
        assertTrue(items.contains("Нижнее белье и носки"));
        assertTrue(items.contains("Предметы личной гигиены"));
        assertTrue(items.contains("Аптечка с необходимыми лекарствами"));
        assertTrue(items.contains("Страховой полис"));
        assertTrue(items.contains("Билеты (электронные копии)"));
        assertTrue(items.contains("Бронь отеля (распечатка)"));
    }

    @Test
    @DisplayName("isSafePrompt - Safe prompt")
    void isSafePrompt_SafePrompt() {
        String prompt = "Поездка на пляж";

        boolean isSafe = service.isSafePrompt(prompt);

        assertTrue(isSafe);
    }

    @Test
    @DisplayName("isSafePrompt - Empty prompt")
    void isSafePrompt_EmptyPrompt() {
        String prompt = "";

        boolean isSafe = service.isSafePrompt(prompt);

        assertFalse(isSafe);
    }

    @Test
    @DisplayName("isSafePrompt - Null prompt")
    void isSafePrompt_NullPrompt() {
        boolean isSafe = service.isSafePrompt(null);

        assertFalse(isSafe);
    }

    @Test
    @DisplayName("isSafePrompt - Banned topic")
    void isSafePrompt_BannedTopic() {
        String prompt = "Поездка с оружием";

        boolean isSafe = service.isSafePrompt(prompt);

        assertFalse(isSafe);
    }

    @Test
    @DisplayName("isSafePrompt - Multiple banned topics")
    void isSafePrompt_MultipleBannedTopics() {
        String prompt = "Поездка с оружием и наркотиками";

        boolean isSafe = service.isSafePrompt(prompt);

        assertFalse(isSafe);
    }
} 