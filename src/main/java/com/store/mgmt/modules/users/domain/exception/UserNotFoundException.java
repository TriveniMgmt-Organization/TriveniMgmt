package com.store.mgmt.modules.users.domain.exception;

import com.store.mgmt.modules.users.domain.model.UserId;

/**
 * Exception thrown when a user is not found.
 */
public class UserNotFoundException extends RuntimeException {

    private final UserId userId;

    public UserNotFoundException(UserId userId) {
        super("User not found: " + userId.getValue());
        this.userId = userId;
    }

    public UserId getUserId() {
        return userId;
    }
}
