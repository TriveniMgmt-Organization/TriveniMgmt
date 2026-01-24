package com.store.mgmt.modules.users.domain.event;

import com.store.mgmt.modules.users.domain.model.UserId;
import com.store.mgmt.shared.domain.event.BaseDomainEvent;

/**
 * Event raised when a new user is created.
 */
public class UserCreated extends BaseDomainEvent {

    private final UserId userId;
    private final String username;
    private final String email;

    public UserCreated(UserId userId, String username, String email) {
        super(userId.getValue(), "User");
        this.userId = userId;
        this.username = username;
        this.email = email;
    }

    public UserId getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }
}
