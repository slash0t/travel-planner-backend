package ru.putevod.app.auth.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Сервис для отправки метрик в Prometheus через Micrometer
 */
@Service
@Slf4j
public class MetricsService {

    private final MeterRegistry meterRegistry;
    private final Map<String, Counter> counters = new ConcurrentHashMap<>();
    private final Map<String, Timer> timers = new ConcurrentHashMap<>();

    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Отслеживание события аутентификации
     */
    public void trackAuthEvent(String eventName, Integer userId, String email, Map<String, Object> parameters) {
        String metricName = "auth_events_total";
        
        Counter.Builder counterBuilder = Counter.builder(metricName)
                .description("Количество событий аутентификации")
                .tag("event", eventName)
                .tag("has_user_id", userId != null ? "true" : "false");
        
        if (email != null) {
            String emailDomain = email.contains("@") ? email.split("@")[1] : "unknown";
            counterBuilder.tag("email_domain", emailDomain);
        }
        
        addParameterTags(counterBuilder, parameters);
        counterBuilder.register(meterRegistry).increment();
        
        log.debug("Tracked auth event: {} for user: {}", eventName, userId);
    }

    /**
     * Отслеживание производительности
     */
    public void trackPerformance(String operation, Integer userId, Map<String, Object> parameters, long durationMs) {
        Counter.builder("operation_total")
                .description("Общее количество операций")
                .tag("operation", operation)
                .tag("status", "success")
                .tag("has_user_id", userId != null ? "true" : "false")
                .register(meterRegistry)
                .increment();

        String timerKey = "operation_duration_" + operation;
        Timer timer = timers.computeIfAbsent(timerKey, key -> 
                Timer.builder("operation_duration_seconds")
                        .description("Время выполнения операций")
                        .tag("operation", operation)
                        .register(meterRegistry));
        
        timer.record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);
        
        log.debug("Tracked performance: {} took {}ms for user: {}", operation, durationMs, userId);
    }

    /**
     * Отслеживание ошибок
     */
    public void trackError(String operation, String errorMessage, Integer userId, Map<String, Object> parameters) {
        Counter.Builder counterBuilder = Counter.builder("errors_total")
                .description("Общее количество ошибок")
                .tag("operation", operation)
                .tag("has_user_id", userId != null ? "true" : "false");
        
        if (errorMessage != null) {
            String errorType = (String) parameters.getOrDefault("error_type", "unknown");
            counterBuilder.tag("error_type", errorType);
        }
        
        addParameterTags(counterBuilder, parameters);
        counterBuilder.register(meterRegistry).increment();
        
        log.debug("Tracked error: {} - {} for user: {}", operation, errorMessage, userId);
    }

    /**
     * Добавление тегов из параметров
     */
    private void addParameterTags(Counter.Builder counterBuilder, Map<String, Object> parameters) {
        if (parameters != null) {
            addTagIfPresent(counterBuilder, parameters, "http_method");
            addTagIfPresent(counterBuilder, parameters, "class");
            
            if (parameters.containsKey("duration_ms")) {
                Object duration = parameters.get("duration_ms");
                if (duration instanceof Number) {
                    long durationMs = ((Number) duration).longValue();
                    String performanceTag = durationMs < 100 ? "fast" : 
                                          durationMs < 1000 ? "medium" : "slow";
                    counterBuilder.tag("performance", performanceTag);
                }
            }
        }
    }

    /**
     * Добавление тега если значение присутствует
     */
    private void addTagIfPresent(Counter.Builder counterBuilder, Map<String, Object> parameters, String key) {
        Object value = parameters.get(key);
        if (value != null) {
            counterBuilder.tag(key, String.valueOf(value));
        }
    }
} 