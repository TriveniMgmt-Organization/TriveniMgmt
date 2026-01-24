package com.store.mgmt.modules.users.domain.model;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value object representing a username.
 */
public record Username(String value) {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_.-]{3,50}$");

    public Username {
        Objects.requireNonNull(value, "Username cannot be null");
        value = value.trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (!USERNAME_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(
                    "Username must be 3-50 characters and contain only letters, numbers, dots, dashes, and underscores"
            );
        }
    }

    public static Username of(String value) {
        return new Username(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
