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

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticationFilter extends OncePerRequestFilter {

    private final AuthServiceClient authServiceClient;

    private static final List<String> AUTH_WHITELIST = Arrays.asList(
            "/api/health",
            "/swagger-ui",
            "/v3/api-docs",
            "/actuator"
    );

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws IOException {
        
        try {
            String path = request.getRequestURI();

            if (isPathWhitelisted(path)) {
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

            String userIdStr = request.getHeader("X-User-Id");
            if (!StringUtils.hasText(userIdStr)) {
                throw new AuthenticationException("Идентификатор пользователя (X-User-Id) отсутствует");
            }

            filterChain.doFilter(request, response);
            
        } catch (AuthenticationException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            log.error("Ошибка в фильтре аутентификации", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Внутренняя ошибка сервера\"}");
        }
    }
    
    /**
     * Извлекает JWT токен из заголовка Authorization
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
     * @param path путь запроса
     * @return true, если путь не требует аутентификации
     */
    private boolean isPathWhitelisted(String path) {
        return AUTH_WHITELIST.stream().anyMatch(path::startsWith);
    }
} 