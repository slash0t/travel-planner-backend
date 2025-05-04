package ru.putevod.app.external.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для инъекции информации о текущем пользователе из JWT токена.
 * Заменяет необходимость ручной обработки заголовка Authorization.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUser {
    /**
     * Указывает, требуется ли полная информация о пользователе (Map) или только userId
     */
    boolean info() default false;
} 