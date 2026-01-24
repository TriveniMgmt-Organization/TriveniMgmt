package com.store.mgmt.modules.users.domain.exception;

import java.util.UUID;

/**
 * Exception thrown when a user is not found.
 */
public class UserNotFoundException extends RuntimeException {

    private final UUID userId;

    public UserNotFoundException(UUID userId) {
        super("User not found: " + userId);
        this.userId = userId;
    }

    public UUID getUserId() {
        return userId;
    }
}
