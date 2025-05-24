package ru.putevod.app.external.service;

import ru.putevod.app.external.dto.response.PixabayResponse;

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
    PixabayResponse getCityImages(String city);
} 