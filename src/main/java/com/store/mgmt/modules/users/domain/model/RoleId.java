package com.store.mgmt.modules.users.domain.model;

import com.store.mgmt.shared.domain.model.Identifier;

import java.util.UUID;

/**
 * Strongly-typed identifier for Role aggregate.
 */
public class RoleId extends Identifier {

    private RoleId(UUID value) {
        super(value);
    }

    public static RoleId of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("RoleId value cannot be null");
        }
        return new RoleId(value);
    }

    public static RoleId generate() {
        return new RoleId(UUID.randomUUID());
    }
}
