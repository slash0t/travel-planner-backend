package ru.putevod.app.external.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для методов, которые доступны только зарегистрированным пользователям
 * Анонимные пользователи получат ошибку 403
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRegisteredUser {

    /**
     * Сообщение об ошибке для анонимных пользователей
     */
    String message() default "Данная функция доступна только зарегистрированным пользователям";
} 