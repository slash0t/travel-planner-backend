package ru.putevod.app.external.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;

@Service
@Slf4j
public class AuthServiceClient {
    private final WebClient webClient;

    @Value("${auth.token}")
    private String serviceToken;

    public AuthServiceClient(@Qualifier("authServiceWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Проверяет валидность токена аутентификации
     *
     * @param token JWT токен для проверки
     * @return true, если токен действителен
     */
    public boolean validateToken(String token) {
        try {
            return webClient.post()
                    .uri("/auth/validate")
                    .bodyValue(new TokenValidationRequest(token))
                    .header("X-Service-Token", serviceToken)
                    .retrieve()
                    .bodyToMono(TokenValidationResponse.class)
                    .map(TokenValidationResponse::valid)
                    .onErrorReturn(false)
                    .block();
        } catch (Exception e) {
            log.error("Ошибка проверки токена: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Получает информацию о пользователе из токена
     *
     * @param token JWT токен для получения информации
     * @return данные пользователя из токена или null в случае ошибки
     */
    public Map<String, Object> getUserInfoFromToken(String token) {
        try {
            return webClient.post()
                    .uri("/auth/userinfo")
                    .bodyValue(new TokenValidationRequest(token))
                    .header("X-Service-Token", serviceToken)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .doOnNext(response -> log.info("Получен ответ от сервиса аутентификации: {}", response))
                    .onErrorResume(e -> {
                        log.error("Ошибка получения информации из токена: {}", e.getMessage(), e);
                        return Mono.just(Collections.emptyMap());
                    })
                    .block();
        } catch (Exception e) {
            log.error("Ошибка получения информации из токена: {}", e.getMessage());
            return null;
        }
    }

    private record TokenValidationRequest(String token) {
    }

    private record TokenValidationResponse(boolean valid) {
    }
} 