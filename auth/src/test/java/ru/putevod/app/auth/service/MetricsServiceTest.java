package ru.putevod.app.auth.service;

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
        Integer userId = 123;
        String email = "test@example.com";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("test_param", "test_value");

        assertDoesNotThrow(() -> {
            metricsService.trackAuthEvent("test_event", userId, email, parameters);
        });

        assertNotNull(meterRegistry.find("auth_events_total").counter());
        assertEquals(1.0, meterRegistry.find("auth_events_total").counter().count());
    }

    @Test
    void testTrackAuthEventWithEmailDomain_shouldHaveEmailDomainTag() {
        Integer userId = 123;
        String email = "test@gmail.com";
        Map<String, Object> parameters = new HashMap<>();

        metricsService.trackAuthEvent("login_success", userId, email, parameters);

        assertNotNull(meterRegistry.find("auth_events_total")
                .tag("email_domain", "gmail.com")
                .counter());
    }

    @Test
    void testTrackAuthEventWithNullEmail_shouldNotHaveEmailDomainTag() {
        Integer userId = 123;
        String email = null;
        Map<String, Object> parameters = new HashMap<>();

        metricsService.trackAuthEvent("login_success", userId, email, parameters);

        assertNotNull(meterRegistry.find("auth_events_total")
                .tag("has_user_id", "true")
                .counter());
    }

    @Test
    void testTrackPerformance_shouldCreateCounterAndTimer() {
        String operation = "login";
        Integer userId = 111;
        Map<String, Object> parameters = new HashMap<>();
        long durationMs = 1500L;

        assertDoesNotThrow(() -> {
            metricsService.trackPerformance(operation, userId, parameters, durationMs);
        });

        assertNotNull(meterRegistry.find("operation_total").counter());
        assertNotNull(meterRegistry.find("operation_duration_seconds").timer());
        assertEquals(1.0, meterRegistry.find("operation_total").counter().count());
    }

    @Test
    void testTrackError_shouldCreateCounter() {
        String operation = "login";
        String errorMessage = "Invalid credentials";
        Integer userId = 999;
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("ip_address", "127.0.0.1");
        parameters.put("error_type", "BadCredentialsException");

        assertDoesNotThrow(() -> {
            metricsService.trackError(operation, errorMessage, userId, parameters);
        });

        assertNotNull(meterRegistry.find("errors_total").counter());
        assertEquals(1.0, meterRegistry.find("errors_total").counter().count());
    }

    @Test
    void testTrackErrorWithNullErrorMessage_shouldStillCreateCounter() {
        String operation = "login";
        String errorMessage = null;
        Integer userId = 999;
        Map<String, Object> parameters = new HashMap<>();

        assertDoesNotThrow(() -> {
            metricsService.trackError(operation, errorMessage, userId, parameters);
        });

        assertNotNull(meterRegistry.find("errors_total").counter());
        assertEquals(1.0, meterRegistry.find("errors_total").counter().count());
    }

    @Test
    void testTrackPerformanceWithNullParameters_shouldStillCreateMetrics() {
        String operation = "login";
        Integer userId = 111;
        Map<String, Object> parameters = null;
        long durationMs = 1500L;

        assertDoesNotThrow(() -> {
            metricsService.trackPerformance(operation, userId, parameters, durationMs);
        });

        assertNotNull(meterRegistry.find("operation_total").counter());
        assertNotNull(meterRegistry.find("operation_duration_seconds").timer());
        assertEquals(1.0, meterRegistry.find("operation_total").counter().count());
    }

    @Test
    void testTrackAuthEventWithNullUserId_shouldHaveCorrectTag() {
        Integer userId = null;
        String email = "test@example.com";
        Map<String, Object> parameters = new HashMap<>();

        metricsService.trackAuthEvent("login_success", userId, email, parameters);

        assertNotNull(meterRegistry.find("auth_events_total")
                .tag("has_user_id", "false")
                .counter());
    }
} 