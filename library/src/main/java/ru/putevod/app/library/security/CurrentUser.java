package ru.putevod.app.library.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUser {
    /**
     * Указывает, требуется ли полная информация о пользователе (UserInfo) или только userId
     */
    boolean info() default false;
} 