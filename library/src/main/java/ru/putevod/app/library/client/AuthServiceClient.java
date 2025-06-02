package ru.putevod.app.library.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceClient {
    private final WebClient authServiceWebClient;

    @Value("${auth.token}")
    private String serviceToken;

    /**
     * Проверяет валидность пользовательского токена через auth-сервис
     *
     * @param token JWT токен пользователя для проверки
     * @return true если токен валидный, false в противном случае
     */
    public boolean validateUserToken(String token) {
        try {
            return Boolean.TRUE.equals(authServiceWebClient.post()
                    .uri("/auth/validate")
                    .bodyValue(new TokenValidationRequest(token))
                    .header("X-Service-Token", serviceToken)
                    .retrieve()
                    .bodyToMono(TokenValidationResponse.class)
                    .map(TokenValidationResponse::isValid)
                    .onErrorReturn(false)
                    .block());
        } catch (Exception e) {
            log.error("Error validating token with auth service", e);
            return false;
        }
    }

    /**
     * Получает информацию о пользователе из токена через auth-сервис
     *
     * @param token JWT токен пользователя
     * @return объект с информацией о пользователе или null в случае ошибки
     */
    public UserInfo getUserInfo(String token) {
        try {
            return authServiceWebClient.post()
                    .uri("/auth/userinfo")
                    .bodyValue(new TokenValidationRequest(token))
                    .header("X-Service-Token", serviceToken)
                    .retrieve()
                    .bodyToMono(UserInfo.class)
                    .onErrorResume(e -> {
                        log.error("Error getting user info from auth service", e);
                        return Mono.empty();
                    })
                    .block();
        } catch (Exception e) {
            log.error("Error getting user info from auth service", e);
            return null;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenValidationRequest {
        private String token;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenValidationResponse {
        private boolean valid;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long userId;
        private String username;
        private String email;
        private boolean isAdmin;
        private String[] roles;
    }
} 