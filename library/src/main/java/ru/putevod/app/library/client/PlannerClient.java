package ru.putevod.app.library.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.putevod.app.library.entity.Trip;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlannerClient {
    @Value("${services.planner.url}")
    private String plannerServiceUrl;

    private final WebClient.Builder webClientBuilder;

    /**
     * Получает детальную информацию о маршруте из сервиса планирования
     *
     * @param tripId id маршрута
     * @param token  токен авторизации из сервиса auth
     * @return данные о маршруте
     */
    public Trip getRouteDetails(Long tripId, String token) {
        WebClient webClient = buildWebClient(token);

        return webClient.get()
                .uri("/trips/{id}", tripId)
                .retrieve()
                .bodyToMono(Trip.class)
                .doOnError(e -> log.error("Error fetching trip details from planner service: {}", e.getMessage()))
                .block();
    }

    /**
     * Проверяет, имеет ли пользователь право на публикацию маршрута
     *
     * @param tripId id маршрута
     * @param userId id пользователя
     * @param token  токен авторизации из сервиса auth
     * @return true, если пользователь может публиковать маршрут
     */
    public boolean canPublishRoute(Long tripId, Long userId, String token) {
        WebClient webClient = buildWebClient(token);

        return Boolean.TRUE.equals(webClient.get()
                .uri("/trips/{id}/can-publish?userId={userId}", tripId, userId)
                .retrieve()
                .bodyToMono(Boolean.class)
                .onErrorReturn(false)
                .block());
    }

    /**
     * Публикует маршрут
     *
     * @param tripId  id маршрута
     * @param userId  id пользователя
     * @param token   токен авторизации из сервиса auth
     * @param publish true для публикации, false для снятия с публикации
     * @return данные обновленного маршрута
     */
    public Trip publishRoute(Long tripId, Long userId, String token, boolean publish) {
        WebClient webClient = buildWebClient(token);

        return webClient.post()
                .uri("/trips/{id}/publish?publish={publish}", tripId, publish)
                .retrieve()
                .bodyToMono(Trip.class)
                .doOnError(e -> log.error("Error publishing route in planner service: {}", e.getMessage()))
                .block();
    }

    /**
     * Создает WebClient с настройками для общения с сервисом планирования
     *
     * @param token токен авторизации
     * @return настроенный WebClient
     */
    private WebClient buildWebClient(String token) {
        return webClientBuilder
                .baseUrl(plannerServiceUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
    }
} 