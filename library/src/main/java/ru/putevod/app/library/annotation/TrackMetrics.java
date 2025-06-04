package ru.putevod.app.library.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для автоматического отслеживания метрик действий пользователей
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TrackMetrics {

    /**
     * Название события (если не указано, будет использовано имя метода)
     */
    String value() default "";

    /**
     * Тип события (auth, planner, external, custom)
     */
    EventType type() default EventType.CUSTOM;

    /**
     * Отслеживать ли производительность
     */
    boolean trackPerformance() default true;

    /**
     * Отслеживать ли ошибки
     */
    boolean trackErrors() default true;

    enum EventType {
        AUTH,
        PLANNER,
        EXTERNAL,
        CUSTOM
    }
} 