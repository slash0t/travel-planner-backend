package ru.putevod.app.auth.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import ru.putevod.app.auth.aspect.MetricsAspect;
import ru.putevod.app.auth.service.MetricsService;

@Configuration
@EnableAspectJAutoProxy
public class MetricsConfig {

    @Bean
    public MetricsService metricsService(MeterRegistry meterRegistry) {
        return new MetricsService(meterRegistry);
    }

    @Bean
    public MetricsAspect metricsAspect(MetricsService metricsService) {
        return new MetricsAspect(metricsService);
    }
} 