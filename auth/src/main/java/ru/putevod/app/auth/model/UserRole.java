package ru.putevod.app.auth.model;

import lombok.Getter;

@Getter
public enum UserRole {
    ANONYMOUS("ROLE_ANONYMOUS"),
    USER("ROLE_USER"),
    ADMIN("ROLE_ADMIN");

    private final String authority;

    UserRole(String authority) {
        this.authority = authority;
    }

    @Override
    public String toString() {
        return authority;
    }
} 