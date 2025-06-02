package ru.putevod.app.external.service;

import java.util.List;
import java.util.Map;

/**
 * Сервис для генерации списков для поездки с помощью AI
 */
public interface AiTripListService {

    /**
     * Генерирует список для поездки на основе текстового запроса
     *
     * @param prompt  Текстовый запрос описывающий поездку
     * @param context Дополнительный контекст (параметры поездки)
     * @return Сгенерированный список элементов
     */
    List<String> generateTripListFromPrompt(String prompt, Map<String, Object> context);

    /**
     * Генерирует список для поездки на основе информации о поездке
     *
     * @param tripId  ID поездки
     * @param context Дополнительный контекст (параметры генерации)
     * @return Сгенерированный список элементов
     */
    List<String> generateTripListFromTrip(Long tripId, Map<String, Object> context);

    /**
     * Генерирует список для поездки на основе шаблона
     *
     * @param templateId ID шаблона
     * @param context    Дополнительный контекст (параметры поездки)
     * @return Сгенерированный список элементов
     */
    List<String> generateTripListFromTemplate(Long templateId, Map<String, Object> context);

    /**
     * Проверяет текстовый запрос на безопасность
     *
     * @param prompt Текстовый запрос для проверки
     * @return true если запрос безопасен, false в противном случае
     */
    boolean isSafePrompt(String prompt);
} 