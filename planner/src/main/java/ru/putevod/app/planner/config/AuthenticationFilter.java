package ru.putevod.app.planner.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.putevod.app.planner.client.AuthServiceClient;
import ru.putevod.app.planner.exception.AuthenticationException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticationFilter extends OncePerRequestFilter {

    private final AuthServiceClient authServiceClient;

    private static final List<String> AUTH_WHITELIST = Arrays.asList(
            "/api/health",
            "/swagger-ui",
            "/v3/api-docs",
            "/api-docs",
            "/api-docs/swagger-config",
            "/actuator"
    );

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            String path = request.getRequestURI();
            log.info("Обработка запроса: {} {}", request.getMethod(), path);

            if (isPathWhitelisted(path)) {
                log.info("Путь в белом списке, пропускаем аутентификацию: {}", path);
                filterChain.doFilter(request, response);
                return;
            }

            String token = getTokenFromRequest(request);

            if (token == null) {
                throw new AuthenticationException("Токен авторизации отсутствует");
            }

            boolean isValid = authServiceClient.validateToken(token);

            if (!isValid) {
                throw new AuthenticationException("Недействительный токен авторизации");
            }

            Map<String, Object> userInfo = authServiceClient.getUserInfoFromToken(token);
            if (userInfo == null || userInfo.isEmpty()) {
                log.error("Получены пустые данные из токена");
                throw new AuthenticationException("Невозможно получить информацию о пользователе из токена");
            }

            // Проверяем, анонимный ли это пользователь
            boolean isAnonymous = Boolean.TRUE.equals(userInfo.get("isAnonymous"));
            
            if (isAnonymous) {
                // Для анонимных пользователей используем anonymousUserId
                Object anonymousUserId = userInfo.get("anonymousUserId");
                if (anonymousUserId != null) {
                    request.setAttribute("userId", Long.valueOf(anonymousUserId.toString()));
                    request.setAttribute("isAnonymous", true);
                    request.setAttribute("deviceId", userInfo.get("deviceId"));
                    log.info("Установлен anonymousUserId = {} для анонимного пользователя", anonymousUserId);
                } else {
                    log.warn("Анонимный пользователь без anonymousUserId в токене: {}", userInfo);
                    // Для обратной совместимости создаем временный ID
                    request.setAttribute("userId", -1L);
                    request.setAttribute("isAnonymous", true);
                    request.setAttribute("deviceId", userInfo.get("deviceId"));
                }
            } else {
                // Для обычных пользователей используем userId
                if (!userInfo.containsKey("userId")) {
                    log.error("Получены данные из токена для обычного пользователя без userId: {}", userInfo);
                    throw new AuthenticationException("Невозможно получить информацию о пользователе из токена");
                }
                
                Long userId = Long.valueOf(userInfo.get("userId").toString());
                request.setAttribute("userId", userId);
                request.setAttribute("isAnonymous", false);
                log.info("Установлен userId = {} для зарегистрированного пользователя", userId);
            }

            log.info("Передаем запрос дальше с атрибутами: userId={}, isAnonymous={}", 
                    request.getAttribute("userId"), request.getAttribute("isAnonymous"));
            filterChain.doFilter(request, response);
            log.info("Запрос обработан filterChain");

        } catch (AuthenticationException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            log.error("Ошибка в фильтре аутентификации", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"error\":\"Внутренняя ошибка сервера\"}");
        }
    }

    /**
     * Извлекает JWT токен из заголовка Authorization
     *
     * @param request HTTP запрос
     * @return токен или null, если токен не найден
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * Проверяет, находится ли путь в белом списке (не требует аутентификации)
     *
     * @param path путь запроса
     * @return true, если путь не требует аутентификации
     */
    private boolean isPathWhitelisted(String path) {
        return AUTH_WHITELIST.stream().anyMatch(path::startsWith);
    }
} 