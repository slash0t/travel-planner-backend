package ru.putevod.app.external.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import ru.putevod.app.external.service.AiTripListService;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Заглушка для сервиса генерации списков для поездки
 * Используется для тестирования и пока не настроен основной сервис
 */
@Service
@Slf4j
public class StubAiTripListService implements AiTripListService {

    private static final List<String> BANNED_TOPICS = Arrays.asList(
            "оружие", "наркотики", "насилие", "терроризм", "детская эксплуатация",
            "weapons", "drugs", "violence", "terrorism", "child exploitation"
    );
    
    @Override
    public List<String> generateTripListFromPrompt(String prompt, Map<String, Object> context) {
        log.info("Генерация списка по запросу: {}, контекст: {}", prompt, context);
        
        if (prompt.toLowerCase().contains("пляж") || prompt.toLowerCase().contains("море")) {
            return Arrays.asList(
                "Плавательные шорты/купальник",
                "Солнцезащитный крем",
                "Пляжное полотенце",
                "Солнцезащитные очки",
                "Шляпа от солнца",
                "Тапочки для пляжа",
                "Надувной матрас/круг",
                "Пляжная сумка",
                "Антисептик для рук",
                "Книга/журнал для чтения"
            );
        } else if (prompt.toLowerCase().contains("горы") || prompt.toLowerCase().contains("поход")) {
            return Arrays.asList(
                "Треккинговые ботинки",
                "Рюкзак",
                "Палатка",
                "Спальный мешок",
                "Карта местности",
                "Компас или GPS",
                "Фонарик и запасные батарейки",
                "Нож или мультитул",
                "Аптечка первой помощи",
                "Термос"
            );
        } else if (prompt.toLowerCase().contains("город") || prompt.toLowerCase().contains("экскурсии")) {
            return Arrays.asList(
                "Удобная обувь для длительных прогулок",
                "Карта города",
                "Путеводитель",
                "Фотоаппарат",
                "Внешний аккумулятор для телефона",
                "Бутылка для воды",
                "Солнцезащитные очки",
                "Легкий рюкзак для дневных прогулок",
                "Зонтик или дождевик",
                "Деньги/карты в разных местах (для безопасности)"
            );
        } else {
            return Arrays.asList(
                "Паспорт и документы",
                "Деньги и банковские карты",
                "Телефон и зарядное устройство",
                "Одежда по сезону",
                "Нижнее белье и носки",
                "Предметы личной гигиены",
                "Аптечка с необходимыми лекарствами",
                "Страховой полис",
                "Билеты (электронные копии)",
                "Бронь отеля (распечатка)"
            );
        }
    }

    @Override
    public List<String> generateTripListFromTrip(Long tripId, Map<String, Object> context) {
        log.info("Генерация списка для поездки с ID {}, контекст: {}", tripId, context);

        return Arrays.asList(
            "Паспорт и документы",
            "Деньги и банковские карты",
            "Телефон и зарядное устройство",
            "Одежда по сезону (проверьте прогноз погоды)",
            "Нижнее белье и носки",
            "Предметы личной гигиены",
            "Аптечка с необходимыми лекарствами",
            "Страховой полис",
            "Билеты (электронные копии)",
            "Бронь отеля (распечатка)"
        );
    }

    @Override
    public List<String> generateTripListFromTemplate(Long templateId, Map<String, Object> context) {
        log.info("Генерация списка на основе шаблона с ID {}, контекст: {}", templateId, context);
        
        if (templateId == 1) {
            return Arrays.asList(
                "Плавательные шорты/купальник",
                "Солнцезащитный крем",
                "Пляжное полотенце",
                "Солнцезащитные очки",
                "Шляпа от солнца",
                "Тапочки для пляжа",
                "Надувной матрас/круг",
                "Пляжная сумка",
                "Антисептик для рук",
                "Книга/журнал для чтения"
            );
        } else if (templateId == 2) {
            return Arrays.asList(
                "Треккинговые ботинки",
                "Рюкзак",
                "Палатка",
                "Спальный мешок",
                "Карта местности",
                "Компас или GPS",
                "Фонарик и запасные батарейки",
                "Нож или мультитул",
                "Аптечка первой помощи",
                "Термос"
            );
        } else {
            return Arrays.asList(
                "Паспорт и документы",
                "Деньги и банковские карты",
                "Телефон и зарядное устройство",
                "Одежда по сезону",
                "Нижнее белье и носки",
                "Предметы личной гигиены",
                "Аптечка с необходимыми лекарствами",
                "Страховой полис",
                "Билеты (электронные копии)",
                "Бронь отеля (распечатка)"
            );
        }
    }

    @Override
    public boolean isSafePrompt(String prompt) {
        if (prompt == null || prompt.trim().isEmpty()) {
            return false;
        }
        
        String lowerPrompt = prompt.toLowerCase();

        for (String bannedTopic : BANNED_TOPICS) {
            if (lowerPrompt.contains(bannedTopic.toLowerCase())) {
                log.warn("Обнаружена запрещенная тема в запросе: {}", bannedTopic);
                return false;
            }
        }
        
        return true;
    }
} 