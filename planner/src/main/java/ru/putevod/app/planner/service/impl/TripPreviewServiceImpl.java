package ru.putevod.app.planner.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.putevod.app.planner.client.ExternalServiceClient;
import ru.putevod.app.planner.dto.external.UnsplashResponseDto;
import ru.putevod.app.planner.service.TripPreviewService;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripPreviewServiceImpl implements TripPreviewService {

    private final ExternalServiceClient externalServiceClient;

    @Value("${trip.default.preview.url:https://images.unsplash.com/photo-1469474968028-56623f02e42e?w=800&h=600&fit=crop&crop=center}")
    private String defaultPreviewUrl;

    @Override
    public String generatePreviewForCity(String city) {
        if (city == null || city.trim().isEmpty()) {
            log.info("Город не указан, используется изображение по умолчанию");
            return getDefaultPreviewUrl();
        }

        UnsplashResponseDto response = externalServiceClient.getCityImages(city);

        if (response == null || response.getResults() == null || response.getResults().isEmpty()) {
            log.info("Для города {} не найдено изображений, используется изображение по умолчанию", city);
            return getDefaultPreviewUrl();
        }

        UnsplashResponseDto.UnsplashImage image = response.getResults().get(0);

        if (image.getUrls() != null) {
            if (image.getUrls().getRegular() != null && !image.getUrls().getRegular().isEmpty()) {
                log.info("Используется regular изображение для города {}: {}", city, image.getUrls().getRegular());
                return image.getUrls().getRegular();
            }

            if (image.getUrls().getSmall() != null && !image.getUrls().getSmall().isEmpty()) {
                log.info("Используется small изображение для города {}: {}", city, image.getUrls().getSmall());
                return image.getUrls().getSmall();
            }

            if (image.getUrls().getThumb() != null && !image.getUrls().getThumb().isEmpty()) {
                log.info("Используется thumb изображение для города {}: {}", city, image.getUrls().getThumb());
                return image.getUrls().getThumb();
            }
        }

        log.info("Для города {} нет подходящих URL изображений, используется изображение по умолчанию", city);
        return getDefaultPreviewUrl();
    }

    @Override
    public String getDefaultPreviewUrl() {
        return defaultPreviewUrl;
    }
} 