package com.store.mgmt.modules.products.domain.model;

import com.store.mgmt.shared.domain.model.ValueObject;

import java.util.Objects;

/**
 * Product barcode (EAN, UPC, etc.).
 */
public final class Barcode implements ValueObject {

    private final String value;

    private Barcode(String value) {
        this.value = value;
    }

    public static Barcode of(String value) {
        if (value == null || value.isBlank()) {
            return null; // Barcode is optional
        }
        String normalized = value.trim();
        if (normalized.length() > 50) {
            throw new IllegalArgumentException("Barcode cannot exceed 50 characters");
        }
        return new Barcode(normalized);
    }

    public static Barcode empty() {
        return null;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Barcode barcode = (Barcode) o;
        return Objects.equals(value, barcode.value);
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
