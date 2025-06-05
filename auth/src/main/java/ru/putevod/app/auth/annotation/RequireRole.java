package ru.putevod.app.auth.annotation;

import ru.putevod.app.auth.model.UserRole;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    UserRole[] value() default {UserRole.USER};

    /**
     * Запрещает доступ анонимным пользователям, даже если их роль указана в value
     */
    boolean denyAnonymous() default false;

    /**
     * Сообщение об ошибке для анонимных пользователей
     */
    String anonymousMessage() default "Для выполнения этого действия необходимо зарегистрироваться";
} 