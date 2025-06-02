package ru.putevod.app.planner.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import ru.putevod.app.planner.dto.external.PixabayResponseDto;

@Service
@Slf4j
public class ExternalServiceClient {

    private final WebClient webClient;

    @Value("${external.service.token:service_token_for_development}")
    private String serviceToken;

    public ExternalServiceClient(@Qualifier("externalServiceWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Получает изображения для указанного города из внешнего сервиса
     *
     * @param city название города
     * @return DTO с информацией об изображениях
     */
    public PixabayResponseDto getCityImages(String city) {
        if (city == null || city.trim().isEmpty()) {
            log.warn("Город не указан для поиска изображений");
            return null;
        }

        try {
            log.info("Запрос изображений для города: {}", city);
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/images/city")
                            .queryParam("city", city)
                            .build())
                    .header("X-Service-Token", serviceToken)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, response -> {
                        log.warn("Сервис изображений вернул ошибку клиента: {}", response.statusCode());
                        return Mono.empty();
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, response -> {
                        log.error("Сервис изображений вернул ошибку сервера: {}", response.statusCode());
                        return Mono.error(new ResponseStatusException(response.statusCode(),
                                "Ошибка сервиса изображений"));
                    })
                    .bodyToMono(PixabayResponseDto.class)
                    .doOnError(e -> log.error("Ошибка получения изображений: {}", e.getMessage()))
                    .onErrorResume(e -> Mono.empty())
                    .block();
        } catch (Exception e) {
            log.error("Ошибка при запросе изображений для города {}: {}", city, e.getMessage());
            return null;
        }
    }
} 