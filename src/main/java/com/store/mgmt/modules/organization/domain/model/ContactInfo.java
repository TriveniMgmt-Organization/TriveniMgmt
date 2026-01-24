package com.store.mgmt.modules.organization.domain.model;

import com.store.mgmt.shared.domain.model.ValueObject;

import java.util.Objects;

/**
 * Value object representing contact information.
 */
public final class ContactInfo implements ValueObject {

    private final String value;

    private ContactInfo(String value) {
        this.value = value;
    }

    public static ContactInfo of(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return new ContactInfo(value.trim());
    }

    public static ContactInfo empty() {
        return null;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContactInfo that = (ContactInfo) o;
        return Objects.equals(value, that.value);
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
