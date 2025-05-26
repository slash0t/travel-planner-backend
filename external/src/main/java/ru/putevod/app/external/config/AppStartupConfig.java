package ru.putevod.app.external.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class AppStartupConfig {
    
    private final AppConfig appConfig;
    
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        checkYandexApiKey();
    }
    
    private void checkYandexApiKey() {
        if (appConfig.getYandexGeoSuggestApiKey() == null || appConfig.getYandexGeoSuggestApiKey().isEmpty()) {
            log.warn("ВНИМАНИЕ: Ключ API Яндекс Геосаджест не настроен!");
            log.warn("Автодополнение мест будет использовать запасной сервис.");
            log.warn("Установите переменную окружения YANDEX_API_GEOSUGGEST_KEY или свойство yandex.geosuggest.api-key в конфигурации.");
        } else {
            log.info("Ключ API Яндекс Геосаджест успешно настроен. Длина ключа: {}", appConfig.getYandexGeoSuggestApiKey().length());
        }
    }
} 