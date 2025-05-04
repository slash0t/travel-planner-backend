package ru.putevod.app.library.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

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
     * Проверяет валидность пользовательского токена через auth-сервис
     * @param token JWT токен пользователя для проверки
     * @return true если токен валидный, false в противном случае
     */
    public boolean validateUserToken(String token) {
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
            log.error("Error validating token with auth service", e);
            return false;
        }
    }

    /**
     * Получает информацию о пользователе из токена через auth-сервис
     * @param token JWT токен пользователя
     * @return объект с информацией о пользователе или null в случае ошибки
     */
    public UserInfo getUserInfo(String token) {
        try {
            return webClient.post()
                    .uri("/auth/userinfo")
                    .bodyValue(new TokenValidationRequest(token))
                    .header("X-Service-Token", serviceToken)
                    .retrieve()
                    .bodyToMono(UserInfo.class)
                    .onErrorReturn(null)
                    .block();
        } catch (Exception e) {
            log.error("Error getting user info from auth service", e);
            return null;
        }
    }

    record TokenValidationRequest(String token) {}

    record TokenValidationResponse(boolean valid) {}

    public record UserInfo(
            Long userId,
            String username,
            String email,
            boolean isAdmin,
            String[] roles
    ) {}
} 