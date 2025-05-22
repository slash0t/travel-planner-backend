package ru.putevod.app.planner.service;

/**
 * Сервис для работы с превью путешествий
 */
public interface TripPreviewService {
    
    /**
     * Генерирует URL превью для путешествия на основе города
     *
     * @param city название города
     * @return URL изображения или URL изображения по умолчанию, если ничего не найдено
     */
    String generatePreviewForCity(String city);
    
    /**
     * Возвращает URL изображения-заглушки
     *
     * @return URL изображения-заглушки
     */
    String getDefaultPreviewUrl();
} 