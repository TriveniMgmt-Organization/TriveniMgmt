package com.store.mgmt.modules.products.domain.model;

import com.store.mgmt.shared.domain.model.ValueObject;

import java.util.Objects;

/**
 * Stock Keeping Unit - unique identifier for a product variant.
 */
public final class Sku implements ValueObject {

    private final String value;

    private Sku(String value) {
        this.value = value;
    }

    public static Sku of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("SKU cannot be null or blank");
        }
        String normalized = value.trim().toUpperCase();
        if (normalized.length() > 50) {
            throw new IllegalArgumentException("SKU cannot exceed 50 characters");
        }
        if (!normalized.matches("^[A-Z0-9\\-_]+$")) {
            throw new IllegalArgumentException("SKU can only contain letters, numbers, hyphens, and underscores");
        }
        return new Sku(normalized);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sku sku = (Sku) o;
        return Objects.equals(value, sku.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
