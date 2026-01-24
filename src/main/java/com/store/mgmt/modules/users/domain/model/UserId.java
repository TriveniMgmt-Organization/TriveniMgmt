package com.store.mgmt.modules.users.domain.model;

import com.store.mgmt.shared.domain.model.Identifier;

import java.util.UUID;

/**
 * Strongly-typed identifier for User aggregate.
 */
public class UserId extends Identifier {

    private UserId(UUID value) {
        super(value);
    }

    public static UserId of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("UserId value cannot be null");
        }
        return new UserId(value);
    }

    public static UserId generate() {
        return new UserId(UUID.randomUUID());
    }
}
