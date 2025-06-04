package ru.putevod.app.auth.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

/**
 * HTTP клиент для взаимодействия с planner сервисом
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlannerClient {

    private final RestTemplate restTemplate;
    
    @Value("${app.services.planner.url}")
    private String plannerServiceUrl;
    
    @Value("${app.auth.service-token}")
    private String serviceToken;

    /**
     * Переносит все данные (путешествия и TODO листы) от анонимного пользователя к зарегистрированному
     *
     * @param anonymousUserId ID анонимного пользователя
     * @param registeredUserId ID зарегистрированного пользователя
     * @return результат миграции
     */
    public Map<String, Object> migrateAllData(Long anonymousUserId, Long registeredUserId) {
        String url = UriComponentsBuilder.fromHttpUrl(plannerServiceUrl + "/api/v1/migration/complete")
                .queryParam("anonymousUserId", anonymousUserId)
                .queryParam("registeredUserId", registeredUserId)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Service-Token", serviceToken);
        
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        log.info("Вызов эндпоинта миграции данных: {} -> {}", anonymousUserId, registeredUserId);
        
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, 
                    HttpMethod.POST, 
                    entity, 
                    Map.class
            );
            
            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = response.getBody();
            
            log.info("Миграция данных выполнена успешно: {}", responseBody);
            return responseBody;
            
        } catch (Exception e) {
            log.error("Ошибка при вызове эндпоинта миграции данных: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка при миграции данных: " + e.getMessage(), e);
        }
    }

    /**
     * Переносит путешествия от анонимного пользователя к зарегистрированному
     *
     * @param anonymousUserId ID анонимного пользователя
     * @param registeredUserId ID зарегистрированного пользователя
     * @return результат переноса путешествий
     */
    public Map<String, Object> transferTripsOwnership(Long anonymousUserId, Long registeredUserId) {
        String url = UriComponentsBuilder.fromHttpUrl(plannerServiceUrl + "/api/v1/migration/trips/transfer")
                .queryParam("anonymousUserId", anonymousUserId)
                .queryParam("registeredUserId", registeredUserId)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Service-Token", serviceToken);
        
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        log.info("Вызов эндпоинта переноса путешествий: {} -> {}", anonymousUserId, registeredUserId);
        
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, 
                    HttpMethod.POST, 
                    entity, 
                    Map.class
            );
            
            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = response.getBody();
            
            log.info("Перенос путешествий выполнен успешно: {}", responseBody);
            return responseBody;
            
        } catch (Exception e) {
            log.error("Ошибка при переносе путешествий: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка при переносе путешествий: " + e.getMessage(), e);
        }
    }

    /**
     * Переносит TODO листы от анонимного пользователя к зарегистрированному
     *
     * @param anonymousUserId ID анонимного пользователя
     * @param registeredUserId ID зарегистрированного пользователя
     * @return результат переноса TODO листов
     */
    public Map<String, Object> transferTodoListsOwnership(Long anonymousUserId, Long registeredUserId) {
        String url = UriComponentsBuilder.fromHttpUrl(plannerServiceUrl + "/api/v1/migration/todo-lists/transfer")
                .queryParam("anonymousUserId", anonymousUserId)
                .queryParam("registeredUserId", registeredUserId)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Service-Token", serviceToken);
        
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        log.info("Вызов эндпоинта переноса TODO листов: {} -> {}", anonymousUserId, registeredUserId);
        
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, 
                    HttpMethod.POST, 
                    entity, 
                    Map.class
            );
            
            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = response.getBody();
            
            log.info("Перенос TODO листов выполнен успешно: {}", responseBody);
            return responseBody;
            
        } catch (Exception e) {
            log.error("Ошибка при переносе TODO листов: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка при переносе TODO листов: " + e.getMessage(), e);
        }
    }
} 