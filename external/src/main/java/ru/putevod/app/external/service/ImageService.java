package ru.putevod.app.external.service;

import ru.putevod.app.external.dto.response.UnsplashResponse;

/**
 * Сервис для работы с изображениями из внешних источников
 */
public interface ImageService {

    /**
     * Получает изображения для указанного города
     *
     * @param city название города
     * @return ответ от API с изображениями
     */
    UnsplashResponse getCityImages(String city);
} 