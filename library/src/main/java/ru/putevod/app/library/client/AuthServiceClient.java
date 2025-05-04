package ru.putevod.app.library.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${services.auth.url}")
    private String authServiceUrl;

    @Value("${auth.token}")
    private String serviceToken;

    /**
     * Проверяет валидность пользовательского токена через auth-сервис
     * @param token JWT токен пользователя для проверки
     * @return true если токен валидный, false в противном случае
     */
    public boolean validateUserToken(String token) {
        try {
            WebClient webClient = buildWebClient();
            
            return Boolean.TRUE.equals(webClient.post()
                    .uri("/api/v1/auth/validate")
                    .bodyValue(new TokenValidationRequest(token))
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
            WebClient webClient = buildWebClient();
            
            return webClient.post()
                    .uri("/api/v1/auth/userinfo")
                    .bodyValue(new TokenValidationRequest(token))
                    .retrieve()
                    .bodyToMono(UserInfo.class)
                    .onErrorReturn(null)
                    .block();
        } catch (Exception e) {
            log.error("Error getting user info from auth service", e);
            return null;
        }
    }

    private WebClient buildWebClient() {
        return webClientBuilder
                .baseUrl(authServiceUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("X-Service-Token", serviceToken)
                .build();
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