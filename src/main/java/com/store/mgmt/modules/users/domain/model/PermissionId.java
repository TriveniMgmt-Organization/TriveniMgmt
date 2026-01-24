package com.store.mgmt.modules.users.domain.model;

import com.store.mgmt.shared.domain.model.Identifier;

import java.util.UUID;

/**
 * Strongly-typed identifier for Permission entity.
 */
public class PermissionId extends Identifier {

    private PermissionId(UUID value) {
        super(value);
    }

    public static PermissionId of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("PermissionId value cannot be null");
        }
        return new PermissionId(value);
    }

    public static PermissionId generate() {
        return new PermissionId(UUID.randomUUID());
    }
}
