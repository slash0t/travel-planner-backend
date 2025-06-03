package ru.putevod.app.auth.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ru.putevod.app.auth.annotation.TrackMetrics;
import ru.putevod.app.auth.service.MetricsService;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Аспект для автоматического отслеживания метрик пользовательских действий в Prometheus
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class MetricsAspect {

    private final MetricsService metricsService;

    @Around("@annotation(trackMetrics)")
    public Object trackMetrics(ProceedingJoinPoint joinPoint, TrackMetrics trackMetrics) throws Throwable {
        long startTime = System.currentTimeMillis();
        Integer userId = getCurrentUserId();
        String methodName = joinPoint.getSignature().getName();
        String eventName = trackMetrics.eventName().isEmpty() ? methodName : trackMetrics.eventName();
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("method", methodName);
        parameters.put("class", joinPoint.getTarget().getClass().getSimpleName());

        collectHttpContextData(parameters);
        
        try {
            Object result = joinPoint.proceed();

            long duration = System.currentTimeMillis() - startTime;
            parameters.put("duration_ms", duration);
            
            // Отслеживаем событие в зависимости от типа
            switch (trackMetrics.type()) {
                case AUTH:
                    metricsService.trackAuthEvent(eventName, userId, null, parameters);
                    break;
                default:
                    log.debug("Неизвестный тип метрики: {}", trackMetrics.type());
            }
            
            metricsService.trackPerformance(eventName, userId, parameters, duration);
            
            return result;
            
        } catch (Throwable throwable) {
            long duration = System.currentTimeMillis() - startTime;
            parameters.put("error_type", throwable.getClass().getSimpleName());
            parameters.put("duration_ms", duration);
            
            metricsService.trackError(methodName, throwable.getMessage(), userId, parameters);
            
            throw throwable;
        }
    }
    
    /**
     * Собирает контекстные данные из HTTP запроса
     */
    private void collectHttpContextData(Map<String, Object> parameters) {
        try {
            ServletRequestAttributes requestAttributes = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            
            if (requestAttributes != null) {
                HttpServletRequest request = requestAttributes.getRequest();

                String ipAddress = request.getRemoteAddr();
                if (ipAddress != null) {
                    parameters.put("ip_address", ipAddress);
                }

                String userAgent = request.getHeader("User-Agent");
                if (userAgent != null) {
                    parameters.put("user_agent", userAgent);
                }

                parameters.put("http_method", request.getMethod());
                parameters.put("request_uri", request.getRequestURI());
            }
        } catch (Exception e) {
            log.debug("Не удалось собрать HTTP контекст: {}", e.getMessage());
        }
    }

    /**
     * Получение текущего пользователя из контекста безопасности
     */
    private Integer getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                if (principal instanceof CustomUserDetails) {
                    return ((CustomUserDetails) principal).getUserId();
                }
            }
        } catch (Exception e) {
            log.debug("Не удалось получить ID пользователя: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Интерфейс для получения ID пользователя из Principal
     */
    public interface CustomUserDetails {
        Integer getUserId();
    }
} 