package ru.putevod.app.library.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MetricsServiceTest {

    private MetricsService metricsService;
    private MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        metricsService = new MetricsService(meterRegistry);
    }

    @Test
    void testTrackAuthEvent_shouldCreateCounter() {
        // Given
        Integer userId = 123;
        String email = "test@example.com";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("test_param", "test_value");

        // When
        assertDoesNotThrow(() -> {
            metricsService.trackAuthEvent("test_event", userId, email, parameters);
        });

        // Then
        assertNotNull(meterRegistry.find("auth_events_total").counter());
        assertEquals(1.0, meterRegistry.find("auth_events_total").counter().count());
    }

    @Test
    void testTrackPlannerEvent_shouldCreateCounter() {
        // Given
        Integer userId = 456;
        String tripId = "trip_789";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("trip_name", "Test Trip");

        // When
        assertDoesNotThrow(() -> {
            metricsService.trackPlannerEvent("test_planner_event", userId, tripId, parameters);
        });

        // Then
        assertNotNull(meterRegistry.find("planner_events_total").counter());
        assertEquals(1.0, meterRegistry.find("planner_events_total").counter().count());
    }

    @Test
    void testTrackExternalEvent_shouldCreateCounter() {
        // Given
        Integer userId = 321;
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("search_query", "hotels");

        // When
        assertDoesNotThrow(() -> {
            metricsService.trackExternalEvent("search_places", userId, parameters);
        });

        // Then
        assertNotNull(meterRegistry.find("external_api_calls_total").counter());
        assertEquals(1.0, meterRegistry.find("external_api_calls_total").counter().count());
    }

    @Test
    void testTrackError_shouldCreateCounter() {
        // Given
        String operation = "login";
        String errorMessage = "Invalid credentials";
        Integer userId = 999;
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("ip_address", "127.0.0.1");
        parameters.put("error_type", "BadCredentialsException");

        // When
        assertDoesNotThrow(() -> {
            metricsService.trackError(operation, errorMessage, userId, parameters);
        });

        // Then
        assertNotNull(meterRegistry.find("errors_total").counter());
        assertEquals(1.0, meterRegistry.find("errors_total").counter().count());
    }

    @Test
    void testTrackPerformance_shouldCreateCounterAndTimer() {
        // Given
        String operation = "login";
        Integer userId = 111;
        Map<String, Object> parameters = new HashMap<>();
        long durationMs = 1500L;

        // When
        assertDoesNotThrow(() -> {
            metricsService.trackPerformance(operation, userId, parameters, durationMs);
        });

        // Then
        assertNotNull(meterRegistry.find("operation_total").counter());
        assertNotNull(meterRegistry.find("operation_duration_seconds").timer());
        assertEquals(1.0, meterRegistry.find("operation_total").counter().count());
    }

    @Test
    void testTrackCustomEvent_shouldCreateCounter() {
        // Given
        String eventName = "user_subscription";
        Integer userId = 222;
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("plan", "premium");
        parameters.put("duration", "yearly");

        // When
        assertDoesNotThrow(() -> {
            metricsService.trackCustomEvent(eventName, userId, parameters);
        });

        // Then
        assertNotNull(meterRegistry.find("custom_events_total").counter());
        assertEquals(1.0, meterRegistry.find("custom_events_total").counter().count());
    }

    @Test
    void testRecordSystemHealth_shouldCreateGauge() {
        // When
        assertDoesNotThrow(() -> {
            metricsService.recordSystemHealth();
        });

        // Then
        assertNotNull(meterRegistry.find("system_health").gauge());
        assertEquals(1.0, meterRegistry.find("system_health").gauge().value());
    }

    @Test
    void testTrackAuthEventWithEmailDomain_shouldHaveEmailDomainTag() {
        // Given
        Integer userId = 123;
        String email = "test@gmail.com";
        Map<String, Object> parameters = new HashMap<>();

        // When
        metricsService.trackAuthEvent("login_success", userId, email, parameters);

        // Then
        assertNotNull(meterRegistry.find("auth_events_total")
                .tag("email_domain", "gmail.com")
                .counter());
    }
} 