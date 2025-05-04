package ru.putevod.app.planner.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.putevod.app.planner.dto.UserDto;
import ru.putevod.app.planner.exception.AuthenticationException;

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
     * Получает информацию о пользователе из сервиса авторизации
     * @param userId ID пользователя
     * @param token JWT токен авторизации
     * @return данные пользователя
     */
    public UserDto getUserInfo(Long userId, String token) {
        return webClient.get()
                .uri("/users/{id}", userId)
                .header("Authorization", "Bearer " + (token != null ? token : serviceToken))
                .retrieve()
                .onStatus(status -> status.equals(HttpStatus.UNAUTHORIZED),
                        response -> Mono.error(new AuthenticationException("Недействительный токен аутентификации")))
                .bodyToMono(UserDto.class)
                .doOnError(e -> log.error("Ошибка получения информации о пользователе из сервиса авторизации: {}", e.getMessage()))
                .block();
    }
    
    /**
     * Проверяет валидность токена аутентификации
     * @param token JWT токен для проверки
     * @return true, если токен действителен
     */
    public boolean validateToken(String token) {
        try {
            return Boolean.TRUE.equals(webClient.get()
                    .uri("/validate-token")
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block());
        } catch (Exception e) {
            log.error("Ошибка проверки токена: {}", e.getMessage());
            return false;
        }
    }
} 