package ru.putevod.app.auth.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;
import ru.putevod.app.auth.annotation.RequireRole;
import ru.putevod.app.auth.model.UserRole;
import ru.putevod.app.auth.security.JwtTokenProvider;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RoleCheckAspect {

    private final JwtTokenProvider tokenProvider;

    @Around("@annotation(requireRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint, RequireRole requireRole) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Невозможно получить контекст запроса");
        }

        HttpServletRequest request = attributes.getRequest();
        String token = extractToken(request);

        if (token == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Токен не предоставлен");
        }

        try {
            if (!tokenProvider.validateToken(token)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Недействительный токен");
            }

            boolean isAnonymous = tokenProvider.isAnonymousToken(token);
            UserRole[] allowedRoles = requireRole.value();

            // Проверяем, запрещен ли доступ анонимным пользователям
            if (isAnonymous && requireRole.denyAnonymous()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, requireRole.anonymousMessage());
            }

            // Проверяем роль
            if (isAnonymous) {
                // Для анонимных пользователей проверяем, есть ли ANONYMOUS в разрешенных ролях
                boolean hasAnonymousRole = Arrays.asList(allowedRoles).contains(UserRole.ANONYMOUS);
                if (!hasAnonymousRole) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Недостаточно прав доступа");
                }
            } else {
                // Для обычных пользователей проверяем их роль
                String email = tokenProvider.getEmailFromToken(token);
                // TODO: Здесь нужно получить роль пользователя из базы данных
                // Пока что предполагаем, что все зарегистрированные пользователи имеют роль USER
                boolean hasUserRole = Arrays.asList(allowedRoles).contains(UserRole.USER);
                if (!hasUserRole) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Недостаточно прав доступа");
                }
            }

            return joinPoint.proceed();

        } catch (Exception e) {
            log.warn("Ошибка при проверке роли: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Ошибка аутентификации");
        }
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
} 