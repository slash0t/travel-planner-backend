package ru.putevod.app.library.aspect;

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
import ru.putevod.app.library.annotation.TrackMetrics;
import ru.putevod.app.library.service.MetricsService;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
        String eventName = trackMetrics.value().isEmpty() ? methodName : trackMetrics.value();
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("method", methodName);
        parameters.put("class", joinPoint.getTarget().getClass().getSimpleName());

        collectHttpContextData(parameters);
        collectMethodParameterData(joinPoint, parameters);
        
        try {
            Object result = joinPoint.proceed();

            collectResultData(result, parameters);

            long duration = System.currentTimeMillis() - startTime;
            
            if (trackMetrics.trackPerformance()) {
                metricsService.trackPerformance(eventName, userId, parameters, duration);
            }
            
            trackEvent(trackMetrics.type(), eventName + "_success", userId, parameters, duration);
            
            return result;
            
        } catch (Throwable throwable) {
            long duration = System.currentTimeMillis() - startTime;
            parameters.put("error_type", throwable.getClass().getSimpleName());
            parameters.put("error_message", throwable.getMessage());
            
            if (trackMetrics.trackErrors()) {
                trackEvent(trackMetrics.type(), eventName + "_error", userId, parameters, duration);
                metricsService.trackError(methodName, throwable.getMessage(), userId, parameters);
            }
            
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
     * Собирает данные из параметров метода
     */
    private void collectMethodParameterData(ProceedingJoinPoint joinPoint, Map<String, Object> parameters) {
        try {
            Object[] args = joinPoint.getArgs();
            String[] paramNames = getParameterNames(joinPoint);
            
            for (int i = 0; i < args.length && i < paramNames.length; i++) {
                Object arg = args[i];
                String paramName = paramNames[i];
                
                if (arg != null) {
                    extractDataFromObject(arg, paramName, parameters);
                }
            }
        } catch (Exception e) {
            log.debug("Не удалось собрать данные параметров: {}", e.getMessage());
        }
    }
    
    /**
     * Извлекает данные из объекта параметра
     */
    private void extractDataFromObject(Object obj, String prefix, Map<String, Object> parameters) {
        try {
            String email = extractFieldValue(obj, "email", String.class);
            if (email != null) {
                parameters.put("user_email", email);
                parameters.put("email_domain", email.split("@")[1]);
            }
            String username = extractFieldValue(obj, "username", String.class);
            if (username != null) {
                parameters.put("username_length", username.length());
            }
            String deviceId = extractFieldValue(obj, "deviceId", String.class);
            if (deviceId != null) {
                parameters.put("has_device_id", true);
            }
            
        } catch (Exception e) {
            log.debug("Не удалось извлечь данные из объекта {}: {}", prefix, e.getMessage());
        }
    }
    
    /**
     * Извлекает значение поля из объекта
     */
    @SuppressWarnings("unchecked")
    private <T> T extractFieldValue(Object obj, String fieldName, Class<T> expectedType) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(obj);
            
            if (expectedType.isInstance(value)) {
                return (T) value;
            }
        } catch (Exception e) {
        }
        return null;
    }
    
    /**
     * Собирает данные из результата выполнения метода
     */
    private void collectResultData(Object result, Map<String, Object> parameters) {
        try {
            if (result != null) {
                Integer userId = extractFieldValue(result, "userId", Integer.class);
                if (userId != null) {
                    parameters.put("result_user_id", userId);
                }
            }
        } catch (Exception e) {
            log.debug("Не удалось собрать данные результата: {}", e.getMessage());
        }
    }
    
    /**
     * Получает имена параметров метода (упрощенная версия)
     */
    private String[] getParameterNames(ProceedingJoinPoint joinPoint) {
        Method method = ((org.aspectj.lang.reflect.MethodSignature) joinPoint.getSignature()).getMethod();
        java.lang.reflect.Parameter[] parameters = method.getParameters();
        String[] names = new String[parameters.length];
        
        for (int i = 0; i < parameters.length; i++) {
            names[i] = parameters[i].getName();
        }
        
        return names;
    }

    private void trackEvent(TrackMetrics.EventType type, String eventName, Integer userId, 
                           Map<String, Object> parameters, Long duration) {
        
        if (duration != null) {
            parameters.put("duration_ms", duration);
        }

        String email = (String) parameters.get("user_email");
        
        switch (type) {
            case AUTH:
                metricsService.trackAuthEvent(eventName, userId, email, parameters);
                break;
            case PLANNER:
                metricsService.trackPlannerEvent(eventName, userId, null, parameters);
                break;
            case EXTERNAL:
                metricsService.trackExternalEvent(eventName, userId, parameters);
                break;
            case CUSTOM:
            default:
                metricsService.trackCustomEvent(eventName, userId, parameters);
                break;
        }
    }

    /**
     * Получение ID текущего пользователя из Security Context
     */
    private Integer getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && 
                !authentication.getName().equals("anonymousUser")) {

                Object principal = authentication.getPrincipal();
                if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                    org.springframework.security.core.userdetails.UserDetails userDetails = 
                        (org.springframework.security.core.userdetails.UserDetails) principal;

                    if (userDetails instanceof CustomUserDetails) {
                        return ((CustomUserDetails) userDetails).getUserId();
                    }

                    try {
                        return Integer.parseInt(authentication.getName());
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Не удалось получить ID пользователя: {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * Интерфейс для кастомного UserDetails с ID пользователя
     */
    public interface CustomUserDetails {
        Integer getUserId();
    }
} 