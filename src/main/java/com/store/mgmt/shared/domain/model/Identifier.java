package com.store.mgmt.shared.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Base class for strongly-typed identifiers.
 * Use this instead of raw UUIDs to prevent mixing up different entity IDs.
 */
public abstract class Identifier implements ValueObject {

    private final UUID value;

    protected Identifier(UUID value) {
        this.value = Objects.requireNonNull(value, "Identifier value cannot be null");
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Identifier that = (Identifier) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + value + ")";
    }
}
