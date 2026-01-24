package com.store.mgmt.modules.users.domain.event;

import com.store.mgmt.modules.users.domain.model.UserId;
import com.store.mgmt.shared.domain.event.BaseDomainEvent;

/**
 * Event raised when a user is deactivated.
 */
public class UserDeactivated extends BaseDomainEvent {

    private final UserId userId;

    public UserDeactivated(UserId userId) {
        super(userId.getValue(), "User");
        this.userId = userId;
    }

    public UserId getUserId() {
        return userId;
    }
}
