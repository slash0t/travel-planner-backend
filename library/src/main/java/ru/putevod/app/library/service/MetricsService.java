package ru.putevod.app.library.service;

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
     * Отслеживание события планировщика
     */
    public void trackPlannerEvent(String eventName, Integer userId, String tripId, Map<String, Object> parameters) {
        String metricName = "planner_events_total";

        Counter.Builder counterBuilder = Counter.builder(metricName)
                .description("Количество событий планировщика")
                .tag("event", eventName)
                .tag("has_user_id", userId != null ? "true" : "false")
                .tag("has_trip_id", tripId != null ? "true" : "false");

        addParameterTags(counterBuilder, parameters);
        counterBuilder.register(meterRegistry).increment();

        log.debug("Tracked planner event: {} for user: {}, trip: {}", eventName, userId, tripId);
    }

    /**
     * Отслеживание внешних API вызовов
     */
    public void trackExternalEvent(String eventName, Integer userId, Map<String, Object> parameters) {
        String metricName = "external_api_calls_total";

        Counter.Builder counterBuilder = Counter.builder(metricName)
                .description("Количество вызовов внешних API")
                .tag("api_call", eventName)
                .tag("has_user_id", userId != null ? "true" : "false");

        addParameterTags(counterBuilder, parameters);
        counterBuilder.register(meterRegistry).increment();

        log.debug("Tracked external API call: {} for user: {}", eventName, userId);
    }

    /**
     * Отслеживание пользовательских событий
     */
    public void trackCustomEvent(String eventName, Integer userId, Map<String, Object> parameters) {
        String metricName = "custom_events_total";

        Counter.Builder counterBuilder = Counter.builder(metricName)
                .description("Количество пользовательских событий")
                .tag("event", eventName)
                .tag("has_user_id", userId != null ? "true" : "false");

        addParameterTags(counterBuilder, parameters);
        counterBuilder.register(meterRegistry).increment();

        log.debug("Tracked custom event: {} for user: {}", eventName, userId);
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
            // Получаем тип ошибки из сообщения или параметров
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
            // Добавляем основные теги из контекста HTTP
            addTagIfPresent(counterBuilder, parameters, "http_method");
            addTagIfPresent(counterBuilder, parameters, "class");

            // Добавляем информацию о результате
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

    /**
     * Получение метрик для здоровья системы
     */
    public void recordSystemHealth() {
        // Записываем базовые метрики системы
        meterRegistry.gauge("system_health", 1.0);

        // Количество активных пользователей (может быть реализовано позже)
        // meterRegistry.gauge("active_users_count", getActiveUsersCount());
    }
} 