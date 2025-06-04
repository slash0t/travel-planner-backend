package ru.putevod.app.auth.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для автоматического отслеживания метрик методов
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TrackMetrics {

    /**
     * Тип метрики (auth, planner, external, custom)
     */
    Type type() default Type.AUTH;

    /**
     * Название события (если не указано, используется имя метода)
     */
    String eventName() default "";

    enum Type {
        AUTH, PLANNER, EXTERNAL, CUSTOM
    }
} 