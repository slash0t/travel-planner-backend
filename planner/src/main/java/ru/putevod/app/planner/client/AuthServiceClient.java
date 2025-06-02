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
     * Получает актуальную информацию о пользователе из сервиса авторизации по ID
     *
     * @param userId ID пользователя
     * @return данные пользователя
     */
    public UserDto getUserById(Long userId) {
        try {
        return webClient.get()
                .uri("/users/{id}", userId)
                    .header("X-Service-Token", serviceToken)
                .retrieve()
                .onStatus(status -> status.equals(HttpStatus.UNAUTHORIZED),
                            response -> Mono.error(new AuthenticationException("Недействительный сервисный токен")))
                    .onStatus(status -> status.equals(HttpStatus.NOT_FOUND),
                            response -> Mono.error(new AuthenticationException("Пользователь не найден")))
                    .bodyToMono(AuthUserInfoDto.class)
                    .map(this::mapToUserDto)
                .doOnError(e -> log.error("Ошибка получения информации о пользователе из сервиса авторизации: {}", e.getMessage()))
                .block();
        } catch (Exception e) {
            log.error("Ошибка при получении пользователя по ID {}: {}", userId, e.getMessage());
            throw new AuthenticationException("Не удалось получить информацию о пользователе");
        }
    }

    /**
     * @deprecated Используйте getUserById(Long userId) для получения актуальных данных пользователя
     */
    @Deprecated
    public UserDto getUserInfo(Long userId, String token) {
        return getUserById(userId);
    }

    /**
     * Проверяет валидность токена аутентификации
     *
     * @param token JWT токен для проверки
     * @return true, если токен действителен
     */
    public boolean validateToken(String token) {
        try {
            return Boolean.TRUE.equals(webClient.post()
                    .uri("/auth/validate")
                    .bodyValue(new TokenValidationRequest(token))
                    .header("X-Service-Token", serviceToken)
                    .retrieve()
                    .bodyToMono(TokenValidationResponse.class)
                    .map(TokenValidationResponse::valid)
                    .onErrorReturn(false)
                    .block());
        } catch (Exception e) {
            log.error("Ошибка проверки токена: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Получает информацию о пользователе из токена (только userId)
     *
     * @param token JWT токен для получения информации
     * @return данные пользователя из токена или null в случае ошибки
     */
    public Map<String, Object> getUserInfoFromToken(String token) {
        try {
            log.info("Запрос информации из токена: {}", token.substring(0, Math.min(10, token.length())) + "...");
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
            log.error("Ошибка получения информации из токена: {}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    /**
     * Преобразует AuthUserInfoDto в UserDto
     */
    private UserDto mapToUserDto(AuthUserInfoDto authUserInfo) {
        return UserDto.builder()
                .id(Long.valueOf(authUserInfo.id()))
                .username(authUserInfo.username())
                .email(authUserInfo.email())
                .admin(authUserInfo.isAdmin())
                .createdAt(authUserInfo.createdAt())
                .profilePictureUrl(authUserInfo.avatarUrl())
                .verified(authUserInfo.emailVerified())
                .build();
    }

    private record TokenValidationRequest(String token) {
    }

    private record TokenValidationResponse(boolean valid) {
    }

    /**
     * DTO для ответа от auth сервиса
     */
    private record AuthUserInfoDto(
            Integer id,
            String email,
            String username,
            String avatarUrl,
            boolean emailVerified,
            boolean isAdmin,
            java.time.LocalDateTime createdAt
    ) {
    }
} 