package ru.putevod.app.planner.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.putevod.app.planner.client.ExternalServiceClient;
import ru.putevod.app.planner.dto.external.PixabayResponseDto;
import ru.putevod.app.planner.service.TripPreviewService;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripPreviewServiceImpl implements TripPreviewService {

    private final ExternalServiceClient externalServiceClient;

    @Value("${trip.default.preview.url:https://via.placeholder.com/800x600?text=Путешествие}")
    private String defaultPreviewUrl;

    @Override
    public String generatePreviewForCity(String city) {
        if (city == null || city.trim().isEmpty()) {
            log.info("Город не указан, используется изображение по умолчанию");
            return getDefaultPreviewUrl();
        }

        PixabayResponseDto response = externalServiceClient.getCityImages(city);

        if (response == null || response.getHits() == null || response.getHits().isEmpty()) {
            log.info("Для города {} не найдено изображений, используется изображение по умолчанию", city);
            return getDefaultPreviewUrl();
        }

        PixabayResponseDto.PixabayImage image = response.getHits().get(0);

        if (image.getLargeImageUrl() != null && !image.getLargeImageUrl().isEmpty()) {
            log.info("Используется большое изображение для города {}: {}", city, image.getLargeImageUrl());
            return image.getLargeImageUrl();
        }

        if (image.getWebformatUrl() != null && !image.getWebformatUrl().isEmpty()) {
            log.info("Используется веб-формат изображения для города {}: {}", city, image.getWebformatUrl());
            return image.getWebformatUrl();
        }

        if (image.getPreviewUrl() != null && !image.getPreviewUrl().isEmpty()) {
            log.info("Используется превью изображения для города {}: {}", city, image.getPreviewUrl());
            return image.getPreviewUrl();
        }

        log.info("Для города {} нет подходящих URL изображений, используется изображение по умолчанию", city);
        return getDefaultPreviewUrl();
    }

    @Override
    public String getDefaultPreviewUrl() {
        return defaultPreviewUrl;
    }
} 